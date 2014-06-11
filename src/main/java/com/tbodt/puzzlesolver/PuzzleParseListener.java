/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tbodt.puzzlesolver;

import com.tbodt.puzzlesolver.PuzzleParser.CategoryDataContext;
import com.tbodt.puzzlesolver.PuzzleParser.CategoryTransformationContext;
import com.tbodt.puzzlesolver.PuzzleParser.FunctionTransformationContext;
import com.tbodt.puzzlesolver.PuzzleParser.StringDataContext;
import java.util.*;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 *
 * @author Theodore Dubois
 */
@SuppressWarnings("null")
public class PuzzleParseListener extends PuzzleBaseListener {

    private final Set<WordSequence> data = new HashSet<>();
    private final List<Transformation> transformations = new ArrayList<>();
    private final ParseTreeProperty<Object> values = new ParseTreeProperty<>();
    private final ANTLRErrorListener errListener;

    public PuzzleParseListener(ANTLRErrorListener errListener) {
        this.errListener = errListener;
    }

    @Override
    public void exitStringData(StringDataContext ctx) {
        data.add(new WordSequence(stripEnds(ctx.STRING().getText())));
    }

    @Override
    public void exitCategoryData(CategoryDataContext ctx) {
        String catName = stripEnds(ctx.CATEGORY().getText());
        Category cat = Category.forName(catName);
        if (cat == null) {
            errListener.syntaxError(null, null, 0, 0, "nonexistent category " + catName, null);
            return;
        }
        data.addAll(cat.getItems());
    }

    @Override
    public void exitCategoryTransformation(CategoryTransformationContext ctx) {
        String catName = stripEnds(ctx.CATEGORY().getText());
        Category cat = Category.forName(catName);
        if (cat == null) {
            errListener.syntaxError(null, null, 0, 0, "nonexistent category " + catName, null);
            return;
        }
        transformations.add(new CategoryTransformation(cat));
    }

    @Override
    public void exitIntValue(PuzzleParser.IntValueContext ctx) {
        values.put(ctx, Integer.valueOf(ctx.INT().getText()));
    }

    @Override
    public void exitStringValue(PuzzleParser.StringValueContext ctx) {
        values.put(ctx, ctx.STRING().getText());
    }

    @Override
    public void exitFunctionTransformation(FunctionTransformationContext ctx) {
        String name = ctx.FUNC().getText();
        List<Object> args = new ArrayList<>(ctx.value());
        args = args.stream().map(vctx -> values.get((ParseTree) vctx)).collect(Collectors.toList());
        Function fun = Function.forName(name);
        if (fun == null) {
            errListener.syntaxError(null, null, 0, 0, "no function with name " + name, null);
            return;
        }
        if (!fun.isValidArguments(args.toArray())) {
            errListener.syntaxError(null, null, 0, 0, "arguments " + args + " invalid", null);
            return;
        }
        transformations.add(new FunctionTransformation(Function.forName(name), args));
    }

    private static String stripEnds(String str) {
        return str.substring(1, str.length() - 1);
    }

    public Set<WordSequence> getData() {
        return Collections.unmodifiableSet(data);
    }

    public List<Transformation> getTransformations() {
        return Collections.unmodifiableList(transformations);
    }

}
