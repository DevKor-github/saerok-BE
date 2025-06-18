package org.devkor.apu.saerok_server.global.exception;

import lombok.Getter;

@Getter
public class OAuthException extends RuntimeException {

  private final int statusCode;

  public OAuthException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }
}
