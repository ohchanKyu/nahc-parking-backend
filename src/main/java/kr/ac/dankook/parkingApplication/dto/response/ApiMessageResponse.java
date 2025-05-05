package kr.ac.dankook.parkingApplication.dto.response;

public record ApiMessageResponse(boolean success, int statusCode, String message) { }
