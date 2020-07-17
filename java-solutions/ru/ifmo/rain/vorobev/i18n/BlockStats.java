package ru.ifmo.rain.vorobev.i18n;

public class BlockStats {
    public final static String nullS = "-";
    public int count = 0;
    public int unique = 0;
    public String minObject = nullS;
    public String maxObject = nullS;
    public int sumLength = 0;
    public String objectMinLength = nullS;
    public String objectMaxLength = nullS;

    public BlockStats(){}

    public BlockStats(int count,int unique,String minObject,String maxObject,int sumLength,String objectMinLength,String objectMaxLength){
        this.count = count;
        this.unique = unique;
        this.minObject = minObject;
        this.maxObject = maxObject;
        this.sumLength = sumLength;
        this.objectMinLength = objectMinLength;
        this.objectMaxLength = objectMaxLength;
    }

    public boolean isEqualTo(BlockStats rightAnswers){
        return (count == rightAnswers.count && unique == rightAnswers.unique && minObject.equals(rightAnswers.minObject)
        && maxObject.equals(rightAnswers.maxObject) && sumLength == rightAnswers.sumLength
        && objectMaxLength.equals(rightAnswers.objectMaxLength)
        && objectMinLength.equals(rightAnswers.objectMinLength));
    }
}
