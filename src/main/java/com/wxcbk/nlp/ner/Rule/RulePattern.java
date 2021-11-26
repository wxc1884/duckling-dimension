package com.wxcbk.nlp.ner.Rule;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RulePattern {
    private static Logger log = LoggerFactory.getLogger(RulePattern.class);
    private static Pattern p = Pattern.compile("(\\{<([^}]+)>})");
    private static Pattern p1 = Pattern.compile("(\\?<([^>]+)>)");

    private List<String> referGroupNames;
    private List<String> regexGroupNames;
    private Pattern pattern;
    private String originRegex;


    public RulePattern(String regex) {
        this.originRegex = regex;
        this.extractGroup();
        if (CollectionUtils.isEmpty(referGroupNames)) {
            try {
                this.pattern = Pattern.compile(originRegex);
            } catch (Exception e) {
                log.error("rule {} compile error:", this.pattern, e);
            }
        }
    }

    public static void main(String[] args) {
        Pattern p = Pattern.compile("([\\d]{1,2}):([\\d]{1,2}):?([\\d]{1,2})?");
        Matcher m = p.matcher("8:50");
        while (m.find()) {
            System.out.println(m.group());
        }
        m.reset();
        System.out.println("===================");
        if (m.find()) {
            for (int i = 0; i <= m.groupCount(); i++) {
                System.out.println(m.group(i));
            }
        }
    }

    private void extractGroup() {
        if (StringUtils.isEmpty(this.originRegex)) {
            return;
        }
        referGroupNames = new ArrayList<>();
        regexGroupNames = new ArrayList<>();
        Matcher m = p.matcher(this.originRegex);
        Matcher m1 = p1.matcher(this.originRegex);
        while (m.find()) {
            referGroupNames.add(m.group(2));
        }
        while (m1.find()) {
            regexGroupNames.add(m1.group(2));
        }
    }

    public List<String> getReferGroupNames() {
        return referGroupNames;
    }

    public void setReferGroupNames(List<String> referGroupNames) {
        this.referGroupNames = referGroupNames;
    }

    public List<String> getRegexGroupNames() {
        return regexGroupNames;
    }

    public void setRegexGroupNames(List<String> regexGroupNames) {
        this.regexGroupNames = regexGroupNames;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public String getOriginRegex() {
        return originRegex;
    }

    public void setOriginRegex(String originRegex) {
        this.originRegex = originRegex;
    }
}
