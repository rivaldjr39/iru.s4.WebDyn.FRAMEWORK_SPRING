package mg.itu.rivaldo.annotation;

import java.util.Base64;

public class Mapfile {
    private String nom;
    private String extension;
    private byte[] data;

    public Mapfile() {}

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    // Taille en octets
    public int getTaille() {
        return data == null ? 0 : data.length;
    }

    // Contenu encodé en Base64 (pour afficher l'image)
    public String getDataBase64() {
        return data == null ? "" : Base64.getEncoder().encodeToString(data);
    }

    // Extension sans le point : ".png" devient "png"
    public String getExtensionSansPoint() {
        return extension == null ? "" : extension.replace(".", "");
    }
}