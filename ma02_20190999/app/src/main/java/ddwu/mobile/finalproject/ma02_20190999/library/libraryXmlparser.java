package ddwu.mobile.finalproject.ma02_20190999.library;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.StringReader;
import java.util.ArrayList;

//공공도서관 xmlparser
public class libraryXmlparser {
    private enum TagType { NONE, LBRRYNM, CTPRVN, CLOSEDAY, RDNMADR, MapX, MapY};

    private final static String ITEM_TAG = "item";
    private final static String LBRRYNM_TAG = "lbrryNm"; //도서관명
    private final static String CTPRVN_TAG = "ctprvnNm"; //시도명
    private final static String CLOSEDAY_TAG = "closeDay"; //휴관일
    private final static String RDNMADR_TAG = "rdnmadr"; //소새지도로명주소
    private final static String MapX_TAG = "latitude"; //위도
    private final static String MapY_TAG = "longitude"; //경도

    private XmlPullParser parser;

    public libraryXmlparser() {
        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<libraryDto> parse(String xml) {

        ArrayList<libraryDto> resultList = new ArrayList();
        libraryDto dto = null;

        TagType tagType = TagType.NONE;

        try {
//            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//            parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

//            태그 유형 구분 변수 준비
            int eventType = parser.getEventType();

//            parsing 수행 - for 문 또는 while 문으로 구성
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equals(ITEM_TAG)) {    // 새로운 항목을 표현하는 태그를 만났을 경우 dto 객체 생성
                            dto = new libraryDto();
                        } else if (parser.getName().equals(LBRRYNM_TAG)) {
                            tagType = TagType.LBRRYNM;
                        }  else if (parser.getName().equals(CTPRVN_TAG)) {
                            tagType = TagType.CTPRVN;
                        }
                        else if (parser.getName().equals(CLOSEDAY_TAG)) {
                            tagType = TagType.CLOSEDAY;
                        }
                        else if (parser.getName().equals(RDNMADR_TAG)) {
                            tagType = TagType.RDNMADR;
                        }
                        else if (parser.getName().equals(MapX_TAG)) {
                            tagType = TagType.MapX;
                        }
                        else if (parser.getName().equals(MapY_TAG)) {
                            tagType = TagType.MapY;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals(ITEM_TAG)) {
                            resultList.add(dto);
                            //dto = null;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        switch(tagType) {       // 태그의 유형에 따라 dto 에 값 저장
                            case LBRRYNM:
                                dto.setLbrryNm(parser.getText());
                                break;
                            case CTPRVN:
                                dto.setCtprvnNm(parser.getText());
                                break;
                            case CLOSEDAY:
                                dto.setCloseDay(parser.getText());
                                break;
                            case RDNMADR:
                                dto.setRdnmadr(parser.getText());
                                break;
                            case MapX:
                                dto.setMapX(parser.getText());
                                break;
                            case MapY:
                                dto.setMapY(parser.getText());
                                break;
                        }
                        tagType = TagType.NONE;
                        break;
                }

                eventType = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }
}