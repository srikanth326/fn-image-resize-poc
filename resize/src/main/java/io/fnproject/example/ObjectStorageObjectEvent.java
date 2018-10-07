package io.fnproject.example;

/**
 * Created by sirksund on 10/6/18.
 */
import lombok.Getter;
import lombok.Setter;

public class ObjectStorageObjectEvent {

    @Getter
    @Setter
    private String tenantId;

    @Getter
    @Setter
    private String bucketOcid;

    @Getter
    @Setter
    private String bucketName;

    @Getter
    @Setter
    private String api;

    @Getter
    @Setter
    private String objectName;

    @Getter
    @Setter
    private String objectEtag;

    @Getter
    @Setter
    private String resourceType;

    @Getter
    @Setter
    private String action;

    @Getter
    @Setter
    private String creationTime;


}
