package cn.udesk.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ProductMessage implements Serializable {


    /**
     * name :  Apple iPhone X (A1903) 64GB 深空灰色 移动联通4G手机
     * url : https://item.jd.com/6748052.html
     * imgUrl : http://img12.360buyimg.com/n1/s450x450_jfs/t10675/253/1344769770/66891/92d54ca4/59df2e7fN86c99a27.jpg
     * params : [{"text":"￥6999.00","color":"#FF0000","fold":false,"break":false,"size":12},{"text":"满1999元另加30元"}]
     */

    /**
     * 商品名称
     */
    private String name;
    /**
     * 商品跳转链接(新页显示)，如果值为空，则不能点击
     */
    private String url;
    /**
     * 商品显示图片的url
     */
    private String imgUrl;

    /**
     * 参数列表
     */
    private List<ParamsBean> params;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public List<ParamsBean> getParams() {
        return params;
    }

    public void setParams(List<ParamsBean> params) {
        this.params = params;
    }

    public static class ParamsBean {
        /**
         * text : ￥6999.00
         * color : #FF0000
         * fold : false
         * break : false
         * size : 12
         */

        /**
         * 参数文本
         */
        private String text;
        /**
         * 参数颜色值，规定为十六进制值的颜色
         */
        private String color;

        /**
         * 是否粗体
         */
        private boolean fold;
        /**
         * 是否换行
         */
        @SerializedName("break")
        private boolean breakX;
        /**
         * 字体大小
         */
        private int size;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public boolean isFold() {
            return fold;
        }

        public void setFold(boolean fold) {
            this.fold = fold;
        }

        public boolean isBreakX() {
            return breakX;
        }

        public void setBreakX(boolean breakX) {
            this.breakX = breakX;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
