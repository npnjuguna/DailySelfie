package com.njuguna.dailyselfie.server.controller;

import com.jhlabs.image.*;
import com.njuguna.dailyselfie.common.ImageFilters;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

@Controller
public class ImageProcessingService {
	
	@RequestMapping(value="/process", method=RequestMethod.GET)
    public @ResponseBody String provideUploadInfo() {
        return "You can upload a file by posting to this same URL.";
    }

    @RequestMapping(value="/process", method=RequestMethod.POST)
    public @ResponseBody ResponseEntity<byte[]> processImage(@RequestParam("token") String token,
                                                                 @RequestParam("type") Integer type,
                                                                 @RequestParam("image") MultipartFile image){
        if (!image.isEmpty()) {
            try {
            	InputStream in = new ByteArrayInputStream(image.getBytes());
            	ByteArrayOutputStream baos = new ByteArrayOutputStream();
            	ImageIO.write( applyFilter(ImageIO.read(in), type),
                        image.getContentType().substring(image.getContentType().lastIndexOf("/") + 1), baos );
                baos.flush();
            	byte[] imageInByte = baos.toByteArray();
            	baos.close();
                HttpHeaders headers = new HttpHeaders();
                headers.set(HttpHeaders.CONTENT_TYPE, image.getContentType());
                return new ResponseEntity<byte[]>(imageInByte, headers, HttpStatus.OK);
            } catch (Exception e) {
            	throw new ImageProcessingException(e.getMessage());
            }
        } else {
            throw new EmptyImageException();
        }
    }

    private BufferedImage applyFilter(BufferedImage originalImage, int filterType) {
        switch (filterType) {
            case ImageFilters.CHANNEL_MIX_FILTER: {
                ChannelMixFilter filter = new ChannelMixFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CONTRAST_FILTER: {
                ContrastFilter filter = new ContrastFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CURVES_FILTER: {
                CurvesFilter filter = new CurvesFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.DIFFUSION_FILTER: {
                DiffusionFilter filter = new DiffusionFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.DITHER_FILTER: {
                DitherFilter filter = new DitherFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.EXPOSURE_FILTER: {
                ExposureFilter filter = new ExposureFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.GAIN_FILTER: {
                GainFilter filter = new GainFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.GAMMA_FILTER: {
                GammaFilter filter = new GammaFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.GRAY_FILTER: {
                GrayFilter filter = new GrayFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.GRAYSCALE_FILTER: {
                GrayscaleFilter filter = new GrayscaleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.HSB_ADJUST_FILTER: {
                HSBAdjustFilter filter = new HSBAdjustFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.INVERT_ALPHA_FILTER: {
                InvertAlphaFilter filter = new InvertAlphaFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.INVERT_FILTER: {
                InvertFilter filter = new InvertFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.LEVELS_FILTER: {
                LevelsFilter filter = new LevelsFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.LOOKUP_FILTER: {
                LookupFilter filter = new LookupFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.MAP_COLORS_FILTER: {
                MapColorsFilter filter = new MapColorsFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.MASK_FILTER: {
                MaskFilter filter = new MaskFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.POSTERIZE_FILTER: {
                PosterizeFilter filter = new PosterizeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.QUANTIZE_FILTER: {
                QuantizeFilter filter = new QuantizeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.RESCALE_FILTER: {
                RescaleFilter filter = new RescaleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.RGB_ADJUST_FILTER: {
                RGBAdjustFilter filter = new RGBAdjustFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SOLARIZE_FILTER: {
                SolarizeFilter filter = new SolarizeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.THRESHOLD_FILTER: {
                ThresholdFilter filter = new ThresholdFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.TRITONE_FILTER: {
                TritoneFilter filter = new TritoneFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.BICUBIC_SCALE_FILTER: {
                BicubicScaleFilter filter = new BicubicScaleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CIRCLE_FILTER: {
                CircleFilter filter = new CircleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CROP_FILTER: {
                CropFilter filter = new CropFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.DIFFUSE_FILTER: {
                DiffuseFilter filter = new DiffuseFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.DISPLACE_FILTER: {
                DiffuseFilter filter = new DiffuseFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.DISSOLVE_FILTER: {
                DissolveFilter filter = new DissolveFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.FIELD_WARP_FILTER: {
                FieldWarpFilter filter = new FieldWarpFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.FLIP_FILTER: {
                FlipFilter filter = new FlipFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.KALEIDOSCOPE_FILTER: {
                KaleidoscopeFilter filter = new KaleidoscopeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.MARBLE_FILTER: {
                MarbleFilter filter = new MarbleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.MIRROR_FILTER: {
                MirrorFilter filter = new MirrorFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.OFFSET_FILTER: {
                OffsetFilter filter = new OffsetFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.PERSPECTIVE_FILTER: {
                PerspectiveFilter filter = new PerspectiveFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.PINCH_FILTER: {
                PinchFilter filter = new PinchFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.POLAR_FILTER: {
                PolarFilter filter = new PolarFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.RIPPLE_FILTER: {
                RippleFilter filter = new RippleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.ROTATE_FILTER: {
                RotateFilter filter = new RotateFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SCALE_FILTER: {
                ScaleFilter filter = new ScaleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SHEAR_FILTER: {
                ShearFilter filter = new ShearFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SPHERE_FILTER: {
                SphereFilter filter = new SphereFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SWIM_FILTER: {
                SwimFilter filter = new SwimFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.TILE_IMAGE_FILTER: {
                TileImageFilter filter = new TileImageFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.TWIRL_FILTER: {
                TwirlFilter filter = new TwirlFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.WARP_FILTER: {
                throw new FeatureNotImplementedException();
            }
            case ImageFilters.WATER_FILTER: {
                WaterFilter filter = new WaterFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.BLOCK_FILTER: {
                BlockFilter filter = new BlockFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.BORDER_FILTER: {
                BorderFilter filter = new BorderFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CHROME_FILTER: {
                ChromeFilter filter = new ChromeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.COLOR_HALFTONE_FILTER: {
                ColorHalftoneFilter filter = new ColorHalftoneFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CRYSTALLIZE_FILTER: {
                CrystallizeFilter filter = new CrystallizeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.EMBOSS_FILTER: {
                EmbossFilter filter = new EmbossFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.FEEDBACK_FILTER: {
                FeedbackFilter filter = new FeedbackFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.HALFTONE_FILTER: {
                HalftoneFilter filter = new HalftoneFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.LIGHT_FILTER: {
                LightFilter filter = new LightFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.NOISE_FILTER: {
                NoiseFilter filter = new NoiseFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.POINTILLIZE_FILTER: {
                PointillizeFilter filter = new PointillizeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SHADOW_FILTER: {
                ShadowFilter filter = new ShadowFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SHAPE_FILTER: {
                ShapeFilter filter = new ShapeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.STAMP_FILTER: {
                StampFilter filter = new StampFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.WEAVE_FILTER: {
                WeaveFilter filter = new WeaveFilter();
                filter.setRoundThreads(false);
                return filter.filter(originalImage, null);
            }
            case ImageFilters.BRUSHED_METAL_FILTER: {
                BrushedMetalFilter filter = new BrushedMetalFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CAUSTICS_FILTER: {
                CausticsFilter filter = new CausticsFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CELLULAR_FILTER: {
                CellularFilter filter = new CellularFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CHECK_FILTER: {
                CheckFilter filter = new CheckFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.FBM_FILTER: {
                FBMFilter filter = new FBMFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.FILL_FILTER: {
                FillFilter filter = new FillFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.FLARE_FILTER: {
                FlareFilter filter = new FlareFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.FOUR_COLOR_FILTER: {
                FourColorFilter filter = new FourColorFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.GRADIENT_FILTER: {
                throw new FeatureNotImplementedException();
            }
            case ImageFilters.PLASMA_FILTER: {
                PlasmaFilter filter = new PlasmaFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.TEXTURE_FILTER: {
                TextureFilter filter = new TextureFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SCRATCH_FILTER: {
                ScratchFilter filter = new ScratchFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SMEAR_FILTER: {
                SmearFilter filter = new SmearFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SPARKLE_FILTER: {
                SparkleFilter filter = new SparkleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.WOOD_FILTER: {
                WoodFilter filter = new WoodFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.BLUR_FILTER: {
                BlurFilter filter = new BlurFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.BOX_BLUR_FILTER: {
                BoxBlurFilter filter = new BoxBlurFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.BUMP_FILTER: {
                BumpFilter filter = new BumpFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.CONVOLVE_FILTER: {
                ConvolveFilter filter = new ConvolveFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.DESPECKLE_FILTER: {
                DespeckleFilter filter = new DespeckleFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.GAUSSIAN_FILTER: {
                GaussianFilter filter = new GaussianFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.GLOW_FILTER: {
                GlowFilter filter = new GlowFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.HIGHPASS_FILTER: {
                HighPassFilter filter = new HighPassFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.LENS_BLUR_FILTER: {
                LensBlurFilter filter = new LensBlurFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.MAXIMUM_FILTER: {
                MaximumFilter filter = new MaximumFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.MEDIAN_FILTER: {
                MedianFilter filter = new MedianFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.MINIMUM_FILTER: {
                MinimumFilter filter = new MinimumFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.MOTION_BLUR_FILTER: {
                MotionBlurFilter filter = new MotionBlurFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.OIL_FILTER: {
                OilFilter filter = new OilFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.RAYS_FILTER: {
                throw new FeatureNotImplementedException();
            }
            case ImageFilters.REDUCE_NOISE_FILTER: {
                ReduceNoiseFilter filter = new ReduceNoiseFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SHARPEN_FILTER: {
                SharpenFilter filter = new SharpenFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.SMART_BLUR_FILTER: {
                SmartBlurFilter filter = new SmartBlurFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.UNSHARP_FILTER: {
                UnsharpFilter filter = new UnsharpFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.VARIABLE_BLUR_FILTER: {
                VariableBlurFilter filter = new VariableBlurFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.DOG_FILTER: {
                throw new FeatureNotImplementedException();
            }
            case ImageFilters.EDGE_FILTER: {
                EdgeFilter filter = new EdgeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.LAPLACE_FILTER: {
                LaplaceFilter filter = new LaplaceFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.BLUR_TRANSITION: {
                throw new FeatureNotImplementedException();
            }
            case ImageFilters.GRADIENT_WIPE_FILTER: {
                GradientWipeFilter filter = new GradientWipeFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.OPACITY_FILTER: {
                OpacityFilter filter = new OpacityFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.PREMULTIPLY_FILTER: {
                PremultiplyFilter filter = new PremultiplyFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.UNPREMULTIPLY_FILTER: {
                UnpremultiplyFilter filter = new UnpremultiplyFilter();
                return filter.filter(originalImage, null);
            }
            case ImageFilters.COMPOUND_FILTER: {
                throw new FeatureNotImplementedException();
            }
            case ImageFilters.ITERATED_FILTER: {
                throw new FeatureNotImplementedException();
            }
        }

        return originalImage;
    }

}

@ControllerAdvice
class ImageProcessingControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ImageProcessingException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    String imageProcessingException(ImageProcessingException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(EmptyImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String emptyImageException(EmptyImageException ex) {
        return ex.getMessage();
    }

}

@SuppressWarnings("serial")
class ImageProcessingException extends RuntimeException {
    public ImageProcessingException(String message) {
        super("Image processing error: '" + message + "'.");
    }
}

@SuppressWarnings("serial")
class EmptyImageException extends RuntimeException {
    public EmptyImageException() {
        super("Image is empty!");
    }
}