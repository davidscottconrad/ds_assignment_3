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
 * CheckoutCart200Response
 */

@JsonTypeName("checkoutCart_200_response")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2026-03-14T11:50:52.847186-07:00[America/Los_Angeles]", comments = "Generator version: 7.20.0")
public class CheckoutCart200Response {

  private @Nullable Integer orderId;

  public CheckoutCart200Response orderId(@Nullable Integer orderId) {
    this.orderId = orderId;
    return this;
  }

  /**
   * Unique identifier for the created order
   * @return orderId
   */
  
  @Schema(name = "order_id", description = "Unique identifier for the created order", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("order_id")
  public @Nullable Integer getOrderId() {
    return orderId;
  }

  public void setOrderId(@Nullable Integer orderId) {
    this.orderId = orderId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CheckoutCart200Response checkoutCart200Response = (CheckoutCart200Response) o;
    return Objects.equals(this.orderId, checkoutCart200Response.orderId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CheckoutCart200Response {\n");
    sb.append("    orderId: ").append(toIndentedString(orderId)).append("\n");
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

