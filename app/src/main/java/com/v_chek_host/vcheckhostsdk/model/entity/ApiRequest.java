package com.v_chek_host.vcheckhostsdk.model.entity;


public class ApiRequest {

    public static class QrUpload {
        final String tenant_id;
        final String site_id;
        final String site_user_id;
        final String tally_master_id;
        final String vehicle_entry_type;
        final String vehicle_image_count;
        final String vehicle_image_url_1;
        final String vehicle_image_url_2;
        final String vehicle_image_url_3;
        final String vehicle_image_url_4;
        final String vehicle_image_url_5;
        final String vin_number;

        public QrUpload(String tenant_id, String site_id, String site_user_id, String tally_master_id,
                        String vehicle_entry_type, String vehicle_image_count, String vehicle_image_url_1,
                        String vehicle_image_url_2, String vehicle_image_url_3, String vehicle_image_url_4,
                        String vehicle_image_url_5, String vin_number) {
            this.tenant_id = tenant_id;
            this.site_id =site_id;
            this.site_user_id = site_user_id;
            this.tally_master_id = tally_master_id;
            this.vehicle_entry_type = vehicle_entry_type;
            this.vehicle_image_count = vehicle_image_count;
            this.vehicle_image_url_1 = vehicle_image_url_1;
            this.vehicle_image_url_2 = vehicle_image_url_2;
            this.vehicle_image_url_3 = vehicle_image_url_3;
            this.vehicle_image_url_4 = vehicle_image_url_4;
            this.vehicle_image_url_5 = vehicle_image_url_5;
            this.vin_number = vin_number;
        }
    }

    public static class GetModel {
        final String v_activity_id;
        final String v_language_id ;
        final String screen_width ;
        final String screen_height ;

        public GetModel(String activityId, String languageId,String width,String height){
            this.v_activity_id  = activityId;
            this.v_language_id  = languageId;
            this.screen_width = width;
            this.screen_height = height;
        }
    }
}
