package free6om.research.qart4j;

import com.google.zxing.common.BitMatrix;
import org.apache.commons.imaging.ImageReadException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by zhangyao on 16-3-2.
 */
public class TestMain {

    public static void main(String[] args) throws IOException, ImageReadException, QArtException {


        /**
         * 经常改变的参数
         *
         * */
        //二维码链接地址
        String url = "http://www.bistu.edu.cn";

        //背景图片
        String filename = "/home/zhangyao/work/ruyi/qart4j/iphone.png";

        //输出路径
        String output = "/home/zhangyao/simple1.png";

        //输出格式
        String outputFormat = "PNG";

        //二维码颜色
        int colorBlack = (int) Long.parseLong("FF000000", 16);

        //二维码背景
        int colorWhite = (int) Long.parseLong("FFFFFFFF", 16);


        // margin
        Integer marginTop = 800;
        Integer marginLeft = 300;
        Integer marginRight = 0;
        Integer marginBottom = 0;


        //背景图片大小
        int width = 577;
        int height =1024;

        //二维码大小
        int size = 168;

        int quietZone = 1; //输入的q 默认2


        //
        int version = 16;



//        输出
        int[][] target;
        int dx = 0;
        int dy = 0;
        int mask = 2;
        int rotation = 0;
        boolean randControl = false;
        long seed = -1L;
        boolean dither = false;
        boolean onlyDataBits = false;
        boolean saveControl = false;


        BufferedImage input = ImageUtil.loadImage(filename, width, height);

        int qrSizeWithoutQuiet = 17 + 4 * version;
        int qrSize = qrSizeWithoutQuiet + quietZone * 2;
        if (size < qrSize) { //don't scale
            size = qrSize;
        }
        int scale = size / qrSize;
        int targetQrSizeWithoutQuiet = qrSizeWithoutQuiet * scale;

        Rectangle inputImageRect = new Rectangle(new Point(0, 0), width, height);
        int startX = 0, startY = 0;
        if (marginLeft != null) {
            startX = marginLeft;
        } else if (marginRight != null) {
            startX = width - marginRight - size;
        }
        if (marginTop != null) {
            startY = marginTop;
        } else if (marginBottom != null) {
            startY = height - marginBottom - size;
        }

        Rectangle qrRect = new Rectangle(new Point(startX, startY), size, size);
        Rectangle qrWithoutQuietRect = new Rectangle(new Point(startX + (size - targetQrSizeWithoutQuiet) / 2, startY + (size - targetQrSizeWithoutQuiet) / 2), targetQrSizeWithoutQuiet, targetQrSizeWithoutQuiet);

        BufferedImage targetImage = null;
        Rectangle targetRect = inputImageRect.intersect(qrWithoutQuietRect);
        if (targetRect == null) {
            target = new int[0][0];
        } else {
            targetImage = input.getSubimage(targetRect.start.x, targetRect.start.y, targetRect.width, targetRect.height);
            int scaledWidth = targetRect.width / scale;
            int scaledHeight = targetRect.height / scale;
            BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics graphics = scaledImage.createGraphics();
            graphics.drawImage(targetImage, 0, 0, scaledWidth, scaledHeight, null);
            graphics.dispose();

            target = ImageUtil.makeTarget(scaledImage, 0, 0, scaledWidth, scaledHeight);
            dx = (qrWithoutQuietRect.start.x - targetRect.start.x) / scale;
            dy = (qrWithoutQuietRect.start.y - targetRect.start.y) / scale;
        }


        Image image = new Image(target, dx, dy, url, version, mask, rotation, randControl, seed, dither, onlyDataBits, saveControl);

        QRCode qrCode = image.encode();
        BitMatrix bitMatrix = ImageUtil.makeBitMatrix(qrCode, quietZone, size);

        MatrixToImageConfig config = new MatrixToImageConfig(colorBlack, colorWhite);
        BufferedImage finalQrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

        Rectangle finalRect = qrRect.union(inputImageRect);
        BufferedImage finalImage = new BufferedImage(finalRect.width, finalRect.height, BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = finalImage.createGraphics();
        graphics.drawImage(input,
                inputImageRect.start.x - finalRect.start.x, inputImageRect.start.y - finalRect.start.y,
                inputImageRect.width, inputImageRect.height, null);
        graphics.drawImage(finalQrImage,
                qrRect.start.x - finalRect.start.x, qrRect.start.y - finalRect.start.y,
                qrRect.width, qrRect.height, null);
        graphics.dispose();

        ImageIO.write(finalImage, outputFormat, new File(output));

    }

}
