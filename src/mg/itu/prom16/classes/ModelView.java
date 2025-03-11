package src.mg.itu.prom16.classes;

import java.util.HashMap;

public class ModelView {
    public String url;
    public HashMap<String, Object> data = new HashMap<>();
    
    public ModelView(String url) {
        this.url = url;
    }
    public ModelView(String url, HashMap<String, Object> data) {
        this.url = url;
        this.data = data;
    }
    public ModelView(String url, String nom, Object object) {
        this.url = url;
        this.data.put(nom, object);
    }
    
    public ModelView() {
    }
    public HashMap<String, Object> getData() {
        return data;
    }public String getUrl() {
        return url;
    } 
    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public void addObject(String nom, Object object) {
        this.data.put(nom, object);
    }

}
