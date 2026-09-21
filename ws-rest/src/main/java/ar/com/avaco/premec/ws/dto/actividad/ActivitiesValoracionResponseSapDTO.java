package ar.com.avaco.premec.ws.dto.actividad;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ActivitiesValoracionResponseSapDTO {

    @JsonProperty("value")
    private List<ActivityValoracionSapDTO> value;

    public List<ActivityValoracionSapDTO> getValue() {
        return value;
    }

    public void setValue(List<ActivityValoracionSapDTO> value) {
        this.value = value;
    }
}