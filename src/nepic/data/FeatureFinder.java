package nepic.data;

public class FeatureFinder {

    // TODO: eventually give data set NOISE here
    // Higher noise --> larger group size (BUT HOW??? --> function?)
    // 0 noise, group size = 1, 100% noise, group size = data set size
    // Totally noise: histogram equalized over all values (no signal)
    // No noise = single column bk, single column feature in histogram
    public FeatureFinder(int groupSize, int[] data) {
        //
    }

    // TODO: absolute v. relative feature significance???
    public void getFeatures(double featureSig) {
        //
    }
}
