package ar.com.avaco.premec.sap.dto;

public class ServiceCallMachineStatsDTO {

    private String maquina;
    private Integer anio;
    private Integer mes;
    private Integer cantidadReclamos;
    private Integer diasParadaTotal;
    private Boolean totalGeneral;

    public String getMaquina() {
        return maquina;
    }

    public void setMaquina(String maquina) {
        this.maquina = maquina;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getCantidadReclamos() {
        return cantidadReclamos;
    }

    public void setCantidadReclamos(Integer cantidadReclamos) {
        this.cantidadReclamos = cantidadReclamos;
    }

    public Integer getDiasParadaTotal() {
        return diasParadaTotal;
    }

    public void setDiasParadaTotal(Integer diasParadaTotal) {
        this.diasParadaTotal = diasParadaTotal;
    }

    public Boolean getTotalGeneral() {
        return totalGeneral;
    }

    public void setTotalGeneral(Boolean totalGeneral) {
        this.totalGeneral = totalGeneral;
    }
}