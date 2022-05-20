package ddwu.mobile.finalproject.ma02_20190999.library;

import java.io.Serializable;

public class libraryDto implements Serializable {

    private long _id;//db저장 기본키
    private String lbrryNm; //도서관명
    private String ctprvnNm;//시도명
    private String closeDay; //휴관일
    private String rdnmadr; //소새지도로명주소
    private String mapX;//위도
    private String mapY;//경도

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String getLbrryNm() {
        return lbrryNm;
    }

    public void setLbrryNm(String lbrryNm) {
        this.lbrryNm = lbrryNm;
    }

    public String getCtprvnNm() {
        return ctprvnNm;
    }

    public void setCtprvnNm(String ctprvnNm) {
        this.ctprvnNm = ctprvnNm;
    }

    public String getCloseDay() {
        return closeDay;
    }

    public void setCloseDay(String closeDay) {
        this.closeDay = closeDay;
    }

    public String getRdnmadr() {
        return rdnmadr;
    }

    public void setRdnmadr(String rdnmadr) {
        this.rdnmadr = rdnmadr;
    }

    public String getMapX() {
        return mapX;
    }

    public void setMapX(String mapX) {
        this.mapX = mapX;
    }

    public String getMapY() {
        return mapY;
    }

    public void setMapY(String mapY) {
        this.mapY = mapY;
    }

    @Override
    public String toString() {
        return "libraryDto{" +
                "_id=" + _id +
                ", lbrryNm='" + lbrryNm + '\'' +
                ", ctprvnNm='" + ctprvnNm + '\'' +
                ", closeDay='" + closeDay + '\'' +
                ", rdnmadr='" + rdnmadr + '\'' +
                ", mapX='" + mapX + '\'' +
                ", mapY='" + mapY + '\'' +
                '}';
    }
}
