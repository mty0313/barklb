package top.mty.barklb.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> {
  private int code;
  private String message;
  private T data;
  private long timestamp;

  // Private constructor to enforce the use of static methods
  private R(int code, String message, T data) {
    this.code = code;
    this.message = message;
    this.data = data;
    this.timestamp = System.currentTimeMillis() / 1000;
  }

  // Static factory method for success response
  public static <T> R<T> success(T data) {
    return new R<>(200, "Success", data);
  }

  // Static factory method for error response
  public static <T> R<T> error(int code, String message, T data) {
    return new R<>(code, message, data);
  }

  @Override
  public String toString() {
    return "R{" +
            "code=" + code +
            ", message='" + message + '\'' +
            ", data=" + data +
            ", timestamp=" + timestamp +
            '}';
  }

  public boolean isSuccess() {
    return this.code == 200;
  }
}
