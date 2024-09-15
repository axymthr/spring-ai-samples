package dev.axymthr.springai.songs;

import org.springframework.ai.image.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class ImageGenController {
    private final ImageModel imageModel;

    public ImageGenController(ImageModel imageModel) {
        this.imageModel = imageModel;
    }
// http -F :8080/imagegen prompt="Dogs playing monopoly" > dogsMonopoly.png
//    http -F :8080/imagegen prompt="Dogs playing monopoly" > dogsMonopoly-dall-e-3.png
    @PostMapping("/imagegen")
    public String imageGen(@RequestBody ImageGenRequest request) {
        // Optional:  Add ImageOptions
//        ImageOptions options = ImageOptionsBuilder.builder()
//                .withModel("dall-e-3")
//                .build();
//        ImagePrompt imagePrompt = new ImagePrompt(request.prompt(), options);
        ImagePrompt imagePrompt = new ImagePrompt(request.prompt());
        ImageResponse response = imageModel.call(imagePrompt);
        String imageUrl = response.getResult().getOutput().getUrl();

        return "redirect:" + imageUrl;
    }



}
