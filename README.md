**Pre-requisite** - package your OCI API private key in src/resources (rename it to oci_api_key.pem)

- `fn create app --annotation oracle.com/oci/subnetIds='["ocid1.subnet.oc1.iad.aaaaaaaaxzyqz3f7wv24i6wtyrgenpdybkaiz6wksszkafxcybeaxlndzptq"]' --config TENANCY=ocid1.tenancy.oc1..aaaaaaaapsj3hr6pl4abnz52jm3wkgf2gfxymbeofzswhcp5jdem3fhjmkeq --config USER=ocid1.user.oc1..aaaaaaaa7grmsqmsx27zuhcqesvb5dvhrwppxtpoxhlvfxvlukuwdypzeg2q --config FINGERPRINT=41:82:5f:44:ca:a1:2e:58:d2:63:6a:af:52:d5:3d:04 --config PASSPHRASE=1987 --config REGION=us-ashburn-1 --config OCI_PRIVATE_KEY_FILE_NAME=oci_api_key.pem --config NAMESPACE=odx-jafar fn-image-resize-app`
- `fn -v deploy --app fn-image-resize-app`
- `fn invoke fn-image-resize-app fn-image-resize`
- Check object store