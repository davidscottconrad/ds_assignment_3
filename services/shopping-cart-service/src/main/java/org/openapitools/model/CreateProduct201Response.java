package org.openapitools.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.springframework.lang.Nullable;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * CreateProduct201Response
 */

@JsonTypeName("createProduct_201_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-14T11:50:52.847186-07:00[America/Los_Angeles]", comments = "Generator version: 7.20.0")
public class CreateProduct201Response {

  private @Nullable Integer productId;

  public CreateProduct201Response productId(@Nullable Integer productId) {
    this.productId = productId;
    return this;
  }

  /**
   * Unique identifier for the created product
   * @return productId
   */
  
  @Schema(name = "product_id", description = "Unique identifier for the created product", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("product_id")
  public @Nullable Integer getProductId() {
    return productId;
  }

  public void setProductId(@Nullable Integer productId) {
    this.productId = productId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreateProduct201Response createProduct201Response = (CreateProduct201Response) o;
    return Objects.equals(this.productId, createProduct201Response.productId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(productId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreateProduct201Response {\n");
    sb.append("    productId: ").append(toIndentedString(productId)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(@Nullable Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

