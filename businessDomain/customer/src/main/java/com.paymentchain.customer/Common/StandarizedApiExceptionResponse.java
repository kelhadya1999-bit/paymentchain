package com.paymentchain.customer.Common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Módulo parar retornan errores según el tipo de excepción")
@NoArgsConstructor
@Data
public class StandarizedApiExceptionResponse {
    @Schema(description = "La url que identifica y categoriza el error", name="type",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "errors/authentification/not-authorized")
    private String type;

    @Schema(description = "Explicación breve sobre el error", name="title",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "El usuario no tiene autorización")
    private String title;

    @Schema(description = "Código único del error", name="code",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "192")
    private String code;

    @Schema(description = "Explicación detallada sobre el error", name="detail",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "El usuario no tiene los permisos necesarios para acceder a este recurso")
    private String detail;

    @Schema(description = "una URI que identifica la específica ocurrencia del error", name="instance",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "errors/authentification/not-authorized/01")
    private String instance;

    public StandarizedApiExceptionResponse(String type, String title, String code, String detail) {
        this.type = type;
        this.title = title;
        this.code = code;
        this.detail = detail;
    }

}
