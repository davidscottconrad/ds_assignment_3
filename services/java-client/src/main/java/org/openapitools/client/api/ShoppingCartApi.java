package org.openapitools.client.api;

import org.openapitools.client.ApiClient;
import org.openapitools.client.BaseApi;

import org.openapitools.client.model.AddItemsToCartRequest;
import org.openapitools.client.model.CheckoutCart200Response;
import org.openapitools.client.model.CheckoutCartRequest;
import org.openapitools.client.model.CreateShoppingCart201Response;
import org.openapitools.client.model.CreateShoppingCartRequest;
import org.openapitools.client.model.Error;

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
public class ShoppingCartApi extends BaseApi {

    public ShoppingCartApi() {
        super(new ApiClient());
    }

    public ShoppingCartApi(ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * Add items to shopping cart
     * Add products with specified quantities to a shopping cart
     * <p><b>204</b> - Items added to cart successfully
     * <p><b>400</b> - Invalid input data
     * <p><b>404</b> - Shopping cart or product not found
     * <p><b>500</b> - Internal server error
     * @param shoppingCartId Unique identifier for the shopping cart (required)
     * @param addItemsToCartRequest  (required)
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void addItemsToCart(Integer shoppingCartId, AddItemsToCartRequest addItemsToCartRequest) throws RestClientException {
        addItemsToCartWithHttpInfo(shoppingCartId, addItemsToCartRequest);
    }

    /**
     * Add items to shopping cart
     * Add products with specified quantities to a shopping cart
     * <p><b>204</b> - Items added to cart successfully
     * <p><b>400</b> - Invalid input data
     * <p><b>404</b> - Shopping cart or product not found
     * <p><b>500</b> - Internal server error
     * @param shoppingCartId Unique identifier for the shopping cart (required)
     * @param addItemsToCartRequest  (required)
     * @return ResponseEntity&lt;Void&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Void> addItemsToCartWithHttpInfo(Integer shoppingCartId, AddItemsToCartRequest addItemsToCartRequest) throws RestClientException {
        Object localVarPostBody = addItemsToCartRequest;
        
        // verify the required parameter 'shoppingCartId' is set
        if (shoppingCartId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'shoppingCartId' when calling addItemsToCart");
        }
        
        // verify the required parameter 'addItemsToCartRequest' is set
        if (addItemsToCartRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'addItemsToCartRequest' when calling addItemsToCart");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("shoppingCartId", shoppingCartId);

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

        ParameterizedTypeReference<Void> localReturnType = new ParameterizedTypeReference<Void>() {};
        return apiClient.invokeAPI("/shopping-carts/{shoppingCartId}/addItem", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Checkout shopping cart
     * Process checkout for a shopping cart
     * <p><b>200</b> - Checkout processed successfully
     * <p><b>400</b> - Invalid shopping cart state
     * <p><b>404</b> - Shopping cart not found
     * <p><b>500</b> - Internal server error
     * @param shoppingCartId Unique identifier for the shopping cart (required)
     * @param checkoutCartRequest  (required)
     * @return CheckoutCart200Response
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public CheckoutCart200Response checkoutCart(Integer shoppingCartId, CheckoutCartRequest checkoutCartRequest) throws RestClientException {
        return checkoutCartWithHttpInfo(shoppingCartId, checkoutCartRequest).getBody();
    }

    /**
     * Checkout shopping cart
     * Process checkout for a shopping cart
     * <p><b>200</b> - Checkout processed successfully
     * <p><b>400</b> - Invalid shopping cart state
     * <p><b>404</b> - Shopping cart not found
     * <p><b>500</b> - Internal server error
     * @param shoppingCartId Unique identifier for the shopping cart (required)
     * @param checkoutCartRequest  (required)
     * @return ResponseEntity&lt;CheckoutCart200Response&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<CheckoutCart200Response> checkoutCartWithHttpInfo(Integer shoppingCartId, CheckoutCartRequest checkoutCartRequest) throws RestClientException {
        Object localVarPostBody = checkoutCartRequest;
        
        // verify the required parameter 'shoppingCartId' is set
        if (shoppingCartId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'shoppingCartId' when calling checkoutCart");
        }
        
        // verify the required parameter 'checkoutCartRequest' is set
        if (checkoutCartRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'checkoutCartRequest' when calling checkoutCart");
        }
        
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("shoppingCartId", shoppingCartId);

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

        ParameterizedTypeReference<CheckoutCart200Response> localReturnType = new ParameterizedTypeReference<CheckoutCart200Response>() {};
        return apiClient.invokeAPI("/shopping-carts/{shoppingCartId}/checkout", HttpMethod.POST, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
    }
    /**
     * Create a new shopping cart
     * Create a new shopping cart for a customer
     * <p><b>201</b> - Shopping cart created successfully
     * <p><b>400</b> - Invalid input data
     * <p><b>500</b> - Internal server error
     * @param createShoppingCartRequest  (required)
     * @return CreateShoppingCart201Response
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public CreateShoppingCart201Response createShoppingCart(CreateShoppingCartRequest createShoppingCartRequest) throws RestClientException {
        return createShoppingCartWithHttpInfo(createShoppingCartRequest).getBody();
    }

    /**
     * Create a new shopping cart
     * Create a new shopping cart for a customer
     * <p><b>201</b> - Shopping cart created successfully
     * <p><b>400</b> - Invalid input data
     * <p><b>500</b> - Internal server error
     * @param createShoppingCartRequest  (required)
     * @return ResponseEntity&lt;CreateShoppingCart201Response&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<CreateShoppingCart201Response> createShoppingCartWithHttpInfo(CreateShoppingCartRequest createShoppingCartRequest) throws RestClientException {
        Object localVarPostBody = createShoppingCartRequest;
        
        // verify the required parameter 'createShoppingCartRequest' is set
        if (createShoppingCartRequest == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'createShoppingCartRequest' when calling createShoppingCart");
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

        ParameterizedTypeReference<CreateShoppingCart201Response> localReturnType = new ParameterizedTypeReference<CreateShoppingCart201Response>() {};
        return apiClient.invokeAPI("/shopping-cart", HttpMethod.POST, Collections.<String, Object>emptyMap(), localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localReturnType);
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
        final String[] localVarContentTypes = { 
            "application/json"
         };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[] { "ApiKeyAuth", "BearerAuth" };

        return apiClient.invokeAPI(localVarPath, method, uriVariables, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarCookieParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, returnType);
    }
}
