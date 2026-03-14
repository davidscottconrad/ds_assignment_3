package org.openapitools.client.api;

import org.openapitools.client.ApiClient;
import org.openapitools.client.BaseApi;

import org.openapitools.client.model.CreateProduct201Response;
import org.openapitools.client.model.Error;
import org.openapitools.client.model.Product;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@javax.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2026-03-14T11:50:35.267105-07:00[America/Los_Angeles]", comments = "Generator version: 7.20.0")
public class ProductApi extends BaseApi {

    public ProductApi() {
        super(new ApiClient());
    }

    public ProductApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * Create a product, with the provided details
     * Create a product, with the provided details. Server creates the new product_id.
     * <p><b>201</b> - Product created successfully
     * <p><b>400</b> - Invalid input data
     * <p><b>500</b> - Internal server error
     * @param product  (required)
     * @return CreateProduct201Response
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public CreateProduct201Response createProduct(Product product) throws RestClientException {
        return createProductWithHttpInfo(product).getBody();
    }

    /**
     * Create a product, with the provided details
     * Create a product, with the provided details. Server creates the new product_id.
     * <p><b>201</b> - Product created successfully
     * <p><b>400</b> - Invalid input data
     * <p><b>500</b> - Internal server error
     * @param product  (required)
     * @return ResponseEntity&lt;CreateProduct201Response&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<CreateProduct201Response> createProductWithHttpInfo(Product product) throws RestClientException {
        Object localVarPostBody = product;
        
        // verify the required parameter 'product' is set
        if (product == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'product' when calling createProduct");
        }
        

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "ApiKeyAuth", "BearerAuth" };

        ParameterizedTypeReference<CreateProduct201Response> localReturnType = new ParameterizedTypeReference<CreateProduct201Response>() {};
        return apiClient.invokeAPI("/product", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Get product by ID
     * Retrieve a product&#39;s details using its unique identifier
     * <p><b>200</b> - Product found successfully
     * <p><b>404</b> - Product not found
     * <p><b>500</b> - Internal server error
     * @param productId Unique identifier for the product (required)
     * @return Product
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public Product getProduct(Integer productId) throws RestClientException {
        return getProductWithHttpInfo(productId).getBody();
    }

    /**
     * Get product by ID
     * Retrieve a product&#39;s details using its unique identifier
     * <p><b>200</b> - Product found successfully
     * <p><b>404</b> - Product not found
     * <p><b>500</b> - Internal server error
     * @param productId Unique identifier for the product (required)
     * @return ResponseEntity&lt;Product&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Product> getProductWithHttpInfo(Integer productId) throws RestClientException {
        Object localVarPostBody = null;
        
        // verify the required parameter 'productId' is set
        if (productId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'productId' when calling getProduct");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("productId", productId);

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "ApiKeyAuth", "BearerAuth" };

        ParameterizedTypeReference<Product> localReturnType = new ParameterizedTypeReference<Product>() {};
        return apiClient.invokeAPI("/products/{productId}", HttpMethod.GET, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }

    @Override
    public <T> ResponseEntity<T> invokeAPI(String url, HttpMethod method, Object request, ParameterizedTypeReference<T> returnType) throws RestClientException {
        String localVarPath = url.replace(apiClient.getBasePath(), "");
        Object localVarPostBody = request;

        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, String> localVarCookieParams = new LinkedMultiValueMap<String, String>();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
         };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {  };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "ApiKeyAuth", "BearerAuth" };

        return apiClient.invokeAPI(localVarPath, method, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, returnType);
    }
}
