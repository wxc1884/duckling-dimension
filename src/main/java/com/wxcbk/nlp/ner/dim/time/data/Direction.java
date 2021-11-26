package com.wxcbk.nlp.ner.dim.time.data;

import com.wxcbk.nlp.ner.Rule.MergeRule;
import com.wxcbk.nlp.ner.Rule.Reference;
import com.wxcbk.nlp.ner.Rule.Rule;
import com.wxcbk.nlp.ner.Rule.RuleParser;
import com.wxcbk.nlp.ner.dim.Dimension;
import com.wxcbk.nlp.ner.dim.time.enums.Hint;
import com.wxcbk.nlp.ner.engine.DimContext;
import com.wxcbk.nlp.ner.token.DimToken;
import com.wxcbk.nlp.ner.util.Lists;
import com.wxcbk.nlp.ner.util.TimeUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Direction extends DimToken implements Dimension, RuleParser {
    private String pos;
    private String direction;

    private static Pattern p = Pattern.compile("(前|后|最近|未来|左右)");

    private static Rule futureOnly = new Rule("futureOnly", "(最?近|未来)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String direction = TimeUtil.extractDirection(m.group(1));
        Direction directT = new Direction(p.getRuleName(), TimeUtil.extractDirectionPos(direction), direction, m);
        directT.setHint(Hint.AUX);
        return directT;
    });

    private static Rule directOnly = new Rule("directOnly", "之?(前|后|左右)", (p) -> {
        Matcher m = p.getPredict().getMatcher();
        String direction = TimeUtil.extractDirection(m.group(1));
        Direction directT = new Direction(p.getRuleName(), TimeUtil.extractDirectionPos(direction), direction, m);
        directT.setHint(Hint.AUX);
        return directT;
    });

    public Direction(String ruleName, String pos, String direction, Matcher m) {
        this.pos = pos;
        this.direction = direction;
        this.setDimName(Direction.class.getSimpleName());
        this.setText(m.group());
        this.setRules(Lists.of(ruleName));
        this.setDirection(direction);
        this.setStart(m.start());
        this.setEnd(m.end());
    }

    public Direction() {
    }

    @Override
    public List<MergeRule> getMergeRule() {
        return null;
    }

    @Override
    public boolean isDimension(DimContext dimContext) {
        Matcher m = p.matcher(dimContext.getQuery());
        return m.find();
    }

    @Override
    public List<String> getDependent() {
        return null;
    }

    @Override
    public List<Reference> initRefer() {
        return null;
    }

    @Override
    public Reference generateRefer(Rule rule, DimContext dimContext) {
        return null;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
