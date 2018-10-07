**Pre-requisite** - place your OCI API private key in `src/resources` directory (rename it to `oci_api_key.pem`)

- Create app (substitute values for your environment) - `fn create app --annotation oracle.com/oci/subnetIds='["ocid1.subnet.oc1.phx.aaaaaaaamapwtgia63r72unzflf6law4ijz4du5vim5fcckk6hyxz2pd4tea"]' --config TENANCY=ocid1.tenancy.oc1..aaaaaaaaltbr5bobenjcbaa3qsuvds6lowqokqzdjllfbwxk5ypjj2e7d23a --config USER=ocid1.user.oc1..aaaaaaaa4wuwj2bv3gybe6zaw5s5sdl5hnv367v7wesywbjczuflzadg5dca --config FINGERPRINT=28:b2:68:2f:a2:bd:75:72:a4:a5:05:13:0a:0c:af:7b --config SOURCE_REGION=us-phoenix-1 --config DEST_REGION=us-ashburn-1 --config OCI_PRIVATE_KEY_FILE_NAME=oci_api_key.pem --config NAMESPACE=ocimiddleware --config SOURCE_BUCKET_NAME=sriks-casper-oow-demo --config DEST_BUCKET_NAME=sriks-casper-oow-demo fn-x-region-image-resize-app`
- Deploy - `fn -v deploy --app fn-x-region-image-resize-app`
- Invoke - `fn invoke fn-x-region-image-resize-app fn-x-region-image-resize`
- .... wait for it ......
- Check object store
