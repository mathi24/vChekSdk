package com.v_chek_host.vcheckhostsdk.model.entity;

public class ModelStatus {


    int _model_id;
    String _model_name;
    int _model_type;
    String _model_iteration;
    int _model_version;
    int _model_width;
    int _model_height;
    String _model_file_name;
    int _model_frame_type;
    String _model_url;
    int _model_download_status;

    public ModelStatus(){

    }

    public ModelStatus(int id){
        this._model_id = id;
    }

    public ModelStatus(int id, String name,int type,int version,int width,int height,
                       String  fileName,String url,int downloadStatus){
        this._model_id = id;
        this._model_name = name;
        this._model_type = type;
        this._model_version = version;
        this._model_width = width;
        this._model_height = height;
        this._model_file_name = fileName;
        this._model_url = url;
        this._model_download_status = downloadStatus;
    }

    public int get_model_id() {
        return _model_id;
    }

    public void set_model_id(int _model_id) {
        this._model_id = _model_id;
    }

    public String get_model_name() {
        return _model_name;
    }

    public void set_model_name(String _model_name) {
        this._model_name = _model_name;
    }

    public int get_model_type() {
        return _model_type;
    }

    public void set_model_type(int _model_type) {
        this._model_type = _model_type;
    }

    public String get_model_iteration() {
        return _model_iteration;
    }

    public void set_model_iteration(String _model_iteration) {
        this._model_iteration = _model_iteration;
    }

    public int get_model_version() {
        return _model_version;
    }

    public void set_model_version(int _model_version) {
        this._model_version = _model_version;
    }

    public int get_model_width() {
        return _model_width;
    }

    public void set_model_width(int _model_width) {
        this._model_width = _model_width;
    }

    public int get_model_height() {
        return _model_height;
    }

    public void set_model_height(int _model_height) {
        this._model_height = _model_height;
    }

    public String get_model_file_name() {
        return _model_file_name;
    }

    public void set_model_file_name(String _model_file_name) {
        this._model_file_name = _model_file_name;
    }

    public int get_model_frame_type() {
        return _model_frame_type;
    }

    public void set_model_frame_type(int _model_frame_type) {
        this._model_frame_type = _model_frame_type;
    }

    public String get_model_url() {
        return _model_url;
    }

    public void set_model_url(String _model_url) {
        this._model_url = _model_url;
    }

    public int get_model_download_status() {
        return _model_download_status;
    }

    public void set_model_download_status(int _model_download_status) {
        this._model_download_status = _model_download_status;
    }

}
