package org.openapitools.api;

import org.openapitools.model.AddItemsToCartRequest;
import org.openapitools.model.CheckoutCart200Response;
import org.openapitools.model.CheckoutCartRequest;
import org.openapitools.model.CreateShoppingCart201Response;
import org.openapitools.model.CreateShoppingCartRequest;
import org.openapitools.model.Error;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.annotation.Generated;

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-14T11:50:51.562192-07:00[America/Los_Angeles]", comments = "Generator version: 7.20.0")
@Controller
@RequestMapping("${openapi.eCommerce.base-path:/v1}")
public class ShoppingCartApiController implements ShoppingCartApi {

    private final NativeWebRequest request;

    @Autowired
    public ShoppingCartApiController(NativeWebRequest request) {
        this.request = request;
    }

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

}
