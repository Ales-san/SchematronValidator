package itworks.group.models;

import java.util.List;

public class StandardResultType {
    private List<List<StandardPropertyType>> list;
    private String result;
    private String resultCode;
    private String resultText;
    private int total;

    public StandardResultType(List<List<StandardPropertyType>> list, String result, String resultCode, String resultText, int total) {
        this.list = list;
        this.result = result;
        this.resultCode = resultCode;
        this.resultText = resultText;
        this.total = total;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultText() {
        return resultText;
    }

    public void setResultText(String resultText) {
        this.resultText = resultText;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<List<StandardPropertyType>> getList() {
        return list;
    }

    public void setList(List<List<StandardPropertyType>> list) {
        this.list = list;
    }
}

