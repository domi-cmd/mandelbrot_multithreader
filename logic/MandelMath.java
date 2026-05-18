package miniproject.mandelbrot_multithreader.logic;

public class MandelMath {
    // Computes a value for a given complex number
    // Formula is from the given template
    public static int calculateFractalPixel(double zRe, double zIm, double pRe, double pIm, int maxIterations) {
        double zRe2 = zRe * zRe;
        double zIm2 = zIm * zIm;
        double zM2 = 0.0;
        int count = 0;
        while (zRe2 + zIm2 < 4.0 && count < maxIterations) {
            zM2 = zRe2 + zIm2;
            zIm = 2.0 * zRe * zIm + pIm;
            zRe = zRe2 - zIm2 + pRe;
            zRe2 = zRe * zRe;
            zIm2 = zIm * zIm;
            count++;
        }
        if (count == 0 || count == maxIterations)
            return 0;
        // transition smoothing
        zM2 += 0.000000001;
        return 256 * count + (int)(255.0 * Math.log(4 / zM2) / Math.log((zRe2 + zIm2) / zM2));
    }
}
