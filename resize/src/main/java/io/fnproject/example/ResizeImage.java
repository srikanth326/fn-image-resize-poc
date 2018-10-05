package io.fnproject.example;

import com.google.common.base.Supplier;

import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;

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

    private ObjectStorage objStoreClient = null;

    public ResizeImage() {
        System.err.println("Inside ResizeImage ctor");
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
            objStoreClient.setRegion(System.getenv().get("REGION"));

            System.err.println("ObjectStorage client setup");
        } catch (Exception ex) {
            //just for better debugging (temporary)
            System.err.println("Error occurred in ResizeImage ctor " + ex.getMessage());
            StringWriter writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            ex.printStackTrace(printWriter);
            ctorEx = writer.toString();
        }
    }

    
    //track exception which occurred in constructor
    String ctorEx = null;

    public String handle() {
        System.err.println("Inside ObjectStorePutFunction/handle");
        String result = "FAILED";

        if (objStoreClient == null) {
            //return result;
            return ctorEx;
        }
        try {

            String nameSpace = System.getenv().getOrDefault("NAMESPACE", "odx-jafar");
            String originalImageName = System.getenv().getOrDefault("IMAGE_NAME", "fn-background-sample.png");
            //String originalImageLocation = System.getenv().getOrDefault("IMAGE_LOC", "/function");
            String bucketName = System.getenv().getOrDefault("BUCKET_NAME", "images");
            int scaleDimension = 400;
            String targetImageType = "jpg";
            String resizedImageName = PREFIX + "-" + originalImageName + "-" + scaleDimension + "-" + new Date() + "." + targetImageType;

            System.out.println("Resizing image " + originalImageName + " to " + resizedImageName);

            InputStream objectData = ResizeImage.class.getResourceAsStream("/" + originalImageName);
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

            PutObjectRequest por = PutObjectRequest.builder()
                    .namespaceName(nameSpace)
                    .bucketName(bucketName)
                    .objectName(resizedImageName)
                    .putObjectBody(resizedImageObject)
                    .build();

            PutObjectResponse poResp = objStoreClient.putObject(por);
            result = "OPC ID for upload operation for object " + resizedImageName + " - " + poResp.getOpcRequestId();
            System.out.println("Pushed to object store " + result);

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
