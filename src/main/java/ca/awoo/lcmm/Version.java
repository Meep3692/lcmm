package ca.awoo.lcmm;

public class Version {
    private int major;
    private int minor;
    private int patch;

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public String toString(){
        return major + "." + minor + "." + patch;
    }
}
