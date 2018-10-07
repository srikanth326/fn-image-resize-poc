package io.fnproject.example;

/**
 * Created by sirksund on 10/6/18.
 */

import com.fnproject.fn.testing.FnResult;
import com.fnproject.fn.testing.FnTestingRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResizeImageTest {
    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();

//    @Test
//    public void testObjectStorage() {
//        final String cloudEvent = "{\"eventType\":\"com.oraclecloud.objectstorage.object.create\",\"eventTypeVersion\":\"1.0\",\"cloudEventsVersion\":\"0.1\",\"source\":\"/service/objectstorage/resourceType/object\",\"eventID\":\"dead-beef-abcd-1234\",\"eventTime\":\"2018-04-12T23:20:50.52Z\",\"extensions\":{\"compartmentId\":\"ocidv1.customerfoo.compartment.abcd\"},\"data\":{\"tenantId\":\"ocid1.tenancy.oc1..aaaaaaaaltbr5bobenjcbaa3qsuvds6lowqokqzdjllfbwxk5ypjj2e7d23a\",\"bucketOcid\":\"ocid1.bucket.oc1.phx.aaaaaaaa7mbpdfjfi6rzz4ef2pu7hhb5vhyf4tmt73d6l4lfa3qkhmyjlljq\",\"bucketName\":\"sriks-casper-oow-demo\",\"api\":\"v2\",\"objectName\":\"Screen_Shot_2018-07-25_at_10.52.30_AM.png\",\"objectEtag\":\"7746B06277BD3795E053824310ACA1F6\",\"resourceType\":\"OBJECT\",\"action\":\"CREATE\",\"creationTime\":\"2018-10-02T21:58:30.070Z\"}}";
//        testing.givenEvent().withBody(cloudEvent).withHeader("content-type", "application/json").enqueue();
//        testing.thenRun(ResizeImage.class, "handle");
//
//        FnResult result = testing.getOnlyResult();
//    }



}
