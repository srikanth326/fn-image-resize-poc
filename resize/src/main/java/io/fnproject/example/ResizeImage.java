package io.fnproject.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Supplier;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;
import com.oracle.pic.events.model.EventV01;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import javax.imageio.ImageIO;

public class ResizeImage {

    public ObjectStorage ConstructObjectStorageClient(ObjectStorageObjectEvent objectStorageObjectEvent, String region) {
        System.out.println("Inside ConstructObjectStorageClient");

        ObjectStorage objStoreClient = null;

        try {
            String privateKey = System.getenv().getOrDefault("OCI_PRIVATE_KEY_FILE_NAME", "oci_api_key.pem");
            System.err.println("Private key " + privateKey);
            Supplier<InputStream> privateKeySupplierFromJAR = () -> {
                return ResizeImage.class.getResourceAsStream("/" + privateKey);
            };

            AuthenticationDetailsProvider provider
                    = SimpleAuthenticationDetailsProvider.builder()
                    .tenantId(System.getenv().get("TENANCY"))
                    .userId(System.getenv().get("USER"))
                    .fingerprint(System.getenv().get("FINGERPRINT"))
                    .passPhrase(System.getenv().get("PASSPHRASE"))
                    .privateKeySupplier(privateKeySupplierFromJAR)
                    .build();

            System.err.println("AuthenticationDetailsProvider setup");

            objStoreClient = new ObjectStorageClient(provider);
            objStoreClient.setRegion(region);
            //objStoreClient.setRegion(System.getenv().get("REGION"));


            System.out.println("ObjectStorage client setup");
        } catch (Exception ex) {
            //just for better debugging (temporary)
            System.err.println("Error occurred in ResizeImage ctor " + ex.getMessage());
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            constructionException = writer.toString();
        }
        return objStoreClient;
    }


    //track exception which occurred in constructor
    String constructionException = null;

    public String handle(EventV01 cloudEvent) {

        System.out.println("Inside ObjectStorePutFunction/handle");
        System.out.println("Event Received: " + cloudEvent.toString());
        String result = "FAILED";

        try {
            ObjectMapper mapper = new ObjectMapper();
            String fromObject = mapper.writeValueAsString(cloudEvent.getData());
            ObjectStorageObjectEvent objectStorageObjectEvent = mapper.readValue(fromObject, ObjectStorageObjectEvent.class);

            String sourceRegion = System.getenv().getOrDefault("SOURCE_REGION", "us-phoenix-1");
            String destRegion = System.getenv().getOrDefault("DEST_REGION", "us-ashburn-1");

            ObjectStorage objStoreClientSource = ConstructObjectStorageClient(objectStorageObjectEvent, sourceRegion);
            if (objStoreClientSource == null) {
                //return result;
                return constructionException;
            }

            ObjectStorage objStoreClientDest = ConstructObjectStorageClient(objectStorageObjectEvent, destRegion);
            if (objStoreClientDest == null) {
                //return result;
                return constructionException;
            }



            // TODO: get the namespace from the event
            String nameSpace = System.getenv().getOrDefault("NAMESPACE", "ocimiddleware");

            // fetch the object
            GetObjectResponse getResponse =
                    objStoreClientSource.getObject(
                            GetObjectRequest.builder()
                                    .namespaceName(nameSpace)
                                    .bucketName(objectStorageObjectEvent.getBucketName())
                                    .objectName(objectStorageObjectEvent.getObjectName())
                                    .build());


            int scaleDimension = 400;
            String targetImageType = "jpg";
            String resizedImageName = PREFIX + "-" + objectStorageObjectEvent.getObjectName() + "-" + scaleDimension + "-" + new Date() + "." + targetImageType;

            System.out.println("Resizing image " + objectStorageObjectEvent.getObjectName() + " to " + resizedImageName);

            InputStream objectData = getResponse.getInputStream();
            BufferedImage srcImage = ImageIO.read(objectData);
            int srcHeight = srcImage.getHeight();
            int srcWidth = srcImage.getWidth();

            System.out.println("Source height and width " + srcHeight + ", " + srcWidth);
            float scalingFactor
                    = Math.min((float) scaleDimension / srcWidth, (float) scaleDimension / srcHeight);

            System.out.println("Scaling factor " + scalingFactor);
            int width = (int) (scalingFactor * srcWidth);
            int height = (int) (scalingFactor * srcHeight);

            System.out.println("Result image height and width " + height + ", " + width);

            BufferedImage resizedImage = scaledImg(srcImage, width, height,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, targetImageType, os);
            InputStream resizedImageObject = new ByteArrayInputStream(os.toByteArray());

            System.out.println("Pushing to object store...");

            String destBucketName = System.getenv().getOrDefault("DEST_BUCKET_NAME", objectStorageObjectEvent.getBucketName());

            PutObjectRequest por = PutObjectRequest.builder()
                    .namespaceName(nameSpace)
                    .bucketName(destBucketName)
                    .objectName(resizedImageName)
                    .putObjectBody(resizedImageObject)
                    .build();

            PutObjectResponse poResp = objStoreClientDest.putObject(por);
            result = "OPC ID for upload operation for object " + resizedImageName + " - " + poResp.getOpcRequestId();
            System.out.println("Pushed to object store " + result + "\n");

        } catch (Exception e) {
            System.err.println("Error invoking object store API " + e.getMessage());
            result = "Error invoking object store API " + e.getMessage();
        }

        return result;
    }

    static String PREFIX = "scaled-";


    public static BufferedImage scaledImg(BufferedImage img, int targetWidth,
                                          int targetHeight, Object hint) {

        System.out.println("scaledImg() start");
        int type = BufferedImage.TYPE_INT_RGB;
        BufferedImage ret = copy(img);
        int w, h;
        w = img.getWidth();
        h = img.getHeight();

        do {
            if (w > targetWidth) {
                w /= 2;
            }
            if (w < targetWidth) {
                w = targetWidth;
            }
            if (h > targetHeight) {
                h /= 2;
            }
            if (h < targetHeight) {
                h = targetHeight;
            }

            BufferedImage tmp = new BufferedImage(w, h, type);
            Graphics2D g2 = tmp.createGraphics();

            g2.setPaint(Color.white);
            g2.fillRect(0, 0, w, h);

            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
            g2.drawImage(ret, 0, 0, w, h, null);
            g2.dispose();

            ret = tmp;
        } while (w != targetWidth || h != targetHeight);

        System.out.println("getHighQualityScaledInstance() end");
        return ret;
    }

    static BufferedImage copy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

}
