import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    private double [] depthsX;
    private int [] maxes;
    /**
     * Constructs a rasterer object. Initializes the depthX array, which is the lonDPP of each
     * depth from 0-7.
     */
    public Rasterer() {
        depthsX = new double [8];
        maxes = new int[8];
        maxes[0] = 1;
        depthsX[0] = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / MapServer.TILE_SIZE;
        for (int i = 1; i < 8; i++) {
            depthsX[i] = depthsX[i - 1] / 2;
            maxes[i] = (int) Math.pow(2, i);
        }
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();
        int depth = depth(params);
        double depthChangeX = (MapServer.ROOT_LRLON - MapServer.ROOT_ULLON) / Math.pow(2, depth);
        double depthChangeY = (MapServer.ROOT_ULLAT - MapServer.ROOT_LRLAT) / Math.pow(2, depth);
        double baseX = MapServer.ROOT_ULLON;
        double baseY = MapServer.ROOT_ULLAT;
        int countFromZeroX = 0;
        while (baseX + depthChangeX < params.get("ullon") && baseX < MapServer.ROOT_LRLON
                && baseX >= MapServer.ROOT_ULLON) {
            baseX += depthChangeX;
            countFromZeroX++;
        }
        double rasterUlLon = baseX;
        int countX = 0;
        while (baseX < params.get("lrlon") && baseX < MapServer.ROOT_LRLON
                && baseX >= MapServer.ROOT_ULLON) {
            baseX += depthChangeX;
            countX++;
        }
        double rasterLrLon = baseX;
        int countFromZeroY = 0;
        while (baseY - depthChangeY > params.get("ullat") && baseY <= MapServer.ROOT_ULLAT
                && baseY > MapServer.ROOT_LRLAT) {
            baseY -= depthChangeY;
            countFromZeroY++;
        }
        double rasterUlLat = baseY;
        int countY = 0;
        while (baseY > params.get("lrlat") && baseY <= MapServer.ROOT_ULLAT
                && baseY > MapServer.ROOT_LRLAT) {
            baseY -= depthChangeY;
            countY++;
        }
        double rasterLrLat = baseY;
        String [][] images = correctImages(countX, countY, countFromZeroX, countFromZeroY, depth);
        results.put("render_grid", images);
        results.put("raster_ul_lon", rasterUlLon);
        results.put("raster_ul_lat", rasterUlLat);
        results.put("raster_lr_lon", rasterLrLon);
        results.put("raster_lr_lat", rasterLrLat);
        results.put("depth", depth);
        results.put("query_success", true);
        return results;
    }

    /**
     * Gets the proper depth for the user's query
     * @param params everything needed for the user's query
     * @return
     */
    private int depth(Map<String, Double> params) {
        double relative = (params.get("lrlon") - params.get("ullon")) / params.get("w");
        int closest = 7;
        for (int i = 0; i < 7; i++) {
            if (depthsX[i] <= relative && depthsX[i] > depthsX[closest]) {
                closest = i;
            }
        }
        return closest;
    }

    /**
     * Uses the number of images from 0, the number of images covering the query, and the depth to
     * get the correct images
     * @param countX the number of x images that covers the query
     * @param countY the number of y images that covers the query
     * @param cfzx the number of x images from 0 to the start of the query
     * @param cfzy the number of y images from 0 to the start of the query
     * @param depth the depth of the rasterized image
     * @return a String[][] of the images used for rasterization
     */
    private String[][] correctImages(int countX, int countY, int cfzx, int cfzy, int depth) {
        int max = maxes[depth];
        int x1 = 0;
        int y1 = 0;
        int starty = cfzy;
        int startx = cfzx;
        if (cfzx < 0) {
            x1 = x1 - cfzx;
            startx = 0;
        }
        if (cfzy < 0) {
            y1 = y1 - cfzy;
            starty = 0;
        }
        if (cfzy > max) {
            y1 = y1 + cfzy;
        }
        if (cfzx > max) {
            x1 = x1 + cfzx;
        }
        String[][] fake = new String[countY - y1][countX - x1];
        int x = 0;
        for (int i = starty; i < starty + countY; i++) {
            int y = 0;
            for (int j = startx; j < startx + countX; j++) {
                if (i < max && j < max && i >= 0 && j >= 0) {
                    fake[x][y] = "d" + depth + "_x" + j + "_y" + i + ".png";
                    y++;
                }
            }
            x++;
        }
        int realX = 0;
        for (int i = 0; i < fake.length; i++) {
            if (fake[i][0] != null) {
                realX++;
            }
        }
        String[][] real = new String[realX][fake[0].length];
        for (int i = 0; i < real.length; i++) {
            for (int j = 0; j < real[0].length; j++) {
                real[i][j] = fake[i][j];
            }
        }
        return real;
    }
}
