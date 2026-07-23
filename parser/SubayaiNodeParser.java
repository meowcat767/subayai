/*
 * Copyright (c) 2022, 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package space.meowcats.subayai.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.source.Source;
import com.oracle.truffle.api.source.SourceSection;
import com.oracle.truffle.api.strings.TruffleString;
import space.meowcats.subayai.SubayaiLanguage;
import space.meowcats.subayai.nodes.SubayaiAstRootNode;
import space.meowcats.subayai.nodes.SubayaiExpressionNode;
import space.meowcats.subayai.nodes.SubayaiRootNode;
import space.meowcats.subayai.nodes.SubayaiStatementNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiBlockNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiBreakNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiContinueNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiDebuggerNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiFunctionBodyNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiIfNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiReturnNode;
import space.meowcats.subayai.nodes.controlflow.SubayaiWhileNode;
import space.meowcats.subayai.nodes.expression.SubayaiAddNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiBigIntegerLiteralNode;
import space.meowcats.subayai.nodes.expression.SubayaiDivNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiEqualNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiFunctionLiteralNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiInvokeNode;
import space.meowcats.subayai.nodes.expression.SubayaiLessOrEqualNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiLessThanNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiLogicalAndNode;
import space.meowcats.subayai.nodes.expression.SubayaiLogicalNotNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiLogicalOrNode;
import space.meowcats.subayai.nodes.expression.SubayaiLongLiteralNode;
import space.meowcats.subayai.nodes.expression.SubayaiMulNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiParenExpressionNode;
import space.meowcats.subayai.nodes.expression.SubayaiReadPropertyNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiStringLiteralNode;
import space.meowcats.subayai.nodes.expression.SubayaiSubNodeGen;
import space.meowcats.subayai.nodes.expression.SubayaiWritePropertyNodeGen;
import space.meowcats.subayai.nodes.local.SubayaiReadArgumentNode;
import space.meowcats.subayai.nodes.local.SubayaiReadLocalVariableNodeGen;
import space.meowcats.subayai.nodes.local.SubayaiWriteLocalVariableNodeGen;
import space.meowcats.subayai.nodes.util.SubayaiUnboxNodeGen;
import space.meowcats.subayai.parser.SubayaiParser.ArithmeticContext;
import space.meowcats.subayai.parser.SubayaiParser.BlockContext;
import space.meowcats.subayai.parser.SubayaiParser.Break_statementContext;
import space.meowcats.subayai.parser.SubayaiParser.Continue_statementContext;
import space.meowcats.subayai.parser.SubayaiParser.Debugger_statementContext;
import space.meowcats.subayai.parser.SubayaiParser.ExpressionContext;
import space.meowcats.subayai.parser.SubayaiParser.Expression_statementContext;
import space.meowcats.subayai.parser.SubayaiParser.FunctionContext;
import space.meowcats.subayai.parser.SubayaiParser.If_statementContext;
import space.meowcats.subayai.parser.SubayaiParser.Logic_factorContext;
import space.meowcats.subayai.parser.SubayaiParser.Logic_termContext;
import space.meowcats.subayai.parser.SubayaiParser.MemberAssignContext;
import space.meowcats.subayai.parser.SubayaiParser.MemberCallContext;
import space.meowcats.subayai.parser.SubayaiParser.MemberFieldContext;
import space.meowcats.subayai.parser.SubayaiParser.MemberIndexContext;
import space.meowcats.subayai.parser.SubayaiParser.Member_expressionContext;
import space.meowcats.subayai.parser.SubayaiParser.NameAccessContext;
import space.meowcats.subayai.parser.SubayaiParser.NumericLiteralContext;
import space.meowcats.subayai.parser.SubayaiParser.ParenExpressionContext;
import space.meowcats.subayai.parser.SubayaiParser.Return_statementContext;
import space.meowcats.subayai.parser.SubayaiParser.StatementContext;
import space.meowcats.subayai.parser.SubayaiParser.StringLiteralContext;
import space.meowcats.subayai.parser.SubayaiParser.TermContext;
import space.meowcats.subayai.parser.SubayaiParser.While_statementContext;

/**
 * Subayai AST visitor that parses to Truffle ASTs.
 */
public class SubayaiNodeParser extends SubayaiBaseParser {

    public static Map<TruffleString, RootCallTarget> parseSubayai(SubayaiLanguage language, Source source) {
        SubayaiNodeParser visitor = new SubayaiNodeParser(language, source);
        parseSubayaiImpl(source, visitor);
        return visitor.functions;
    }

    private FrameDescriptor.Builder frameDescriptorBuilder;

    private SubayaiStatementVisitor statementVisitor = new SubayaiStatementVisitor();
    private SubayaiExpressionVisitor expressionVisitor = new SubayaiExpressionVisitor();
    private int loopDepth = 0;
    private final Map<TruffleString, RootCallTarget> functions = new HashMap<>();

    protected SubayaiNodeParser(SubayaiLanguage language, Source source) {
        super(language, source);
    }

    @Override
    public Void visitFunction(FunctionContext ctx) {

        Token nameToken = ctx.IDENTIFIER(0).getSymbol();

        TruffleString functionName = asTruffleString(nameToken, false);

        int functionStartPos = nameToken.getStartIndex();
        frameDescriptorBuilder = FrameDescriptor.newBuilder();
        List<SubayaiStatementNode> methodNodes = new ArrayList<>();

        int parameterCount = enterFunction(ctx).size();

        for (int i = 0; i < parameterCount; i++) {
            Token paramToken = ctx.IDENTIFIER(i + 1).getSymbol();

            TruffleString paramName = asTruffleString(paramToken, false);
            int localIndex = frameDescriptorBuilder.addSlot(FrameSlotKind.Illegal, paramName, null);
            assert localIndex == i;

            final SubayaiReadArgumentNode readArg = new SubayaiReadArgumentNode(i);
            readArg.setSourceSection(paramToken.getStartIndex(), paramToken.getText().length());
            SubayaiExpressionNode assignment = createAssignment(createString(paramToken, false), readArg, i);
            methodNodes.add(assignment);
        }

        SubayaiBlockNode bodyNode = (SubayaiBlockNode) statementVisitor.visitBlock(ctx.body);

        exitFunction();

        methodNodes.addAll(bodyNode.getStatements());
        final int bodyEndPos = bodyNode.getSourceEndIndex();
        final SourceSection functionSrc = source.createSection(functionStartPos, bodyEndPos - functionStartPos);
        final SubayaiStatementNode methodBlock = new SubayaiBlockNode(methodNodes.toArray(new SubayaiStatementNode[methodNodes.size()]));
        methodBlock.setSourceSection(functionStartPos, bodyEndPos - functionStartPos);

        final SubayaiFunctionBodyNode functionBodyNode = new SubayaiFunctionBodyNode(methodBlock);
        functionBodyNode.setSourceSection(functionSrc.getCharIndex(), functionSrc.getCharLength());

        final SubayaiRootNode rootNode = new SubayaiAstRootNode(language, frameDescriptorBuilder.build(), functionBodyNode, functionSrc, functionName);
        functions.put(functionName, rootNode.getCallTarget());

        frameDescriptorBuilder = null;

        return null;
    }

    private SubayaiStringLiteralNode createString(Token name, boolean removeQuotes) {
        SubayaiStringLiteralNode node = new SubayaiStringLiteralNode(asTruffleString(name, removeQuotes));
        node.setSourceSection(name.getStartIndex(), name.getStopIndex() - name.getStartIndex() + 1);
        return node;
    }

    private class SubayaiStatementVisitor extends SubayaiBaseVisitor<SubayaiStatementNode> {
        @Override
        public SubayaiStatementNode visitBlock(BlockContext ctx) {
            List<TruffleString> newLocals = enterBlock(ctx);

            for (TruffleString newLocal : newLocals) {
                frameDescriptorBuilder.addSlot(FrameSlotKind.Illegal, newLocal, null);
            }

            int startPos = ctx.s.getStartIndex();
            int endPos = ctx.e.getStopIndex() + 1;

            List<SubayaiStatementNode> bodyNodes = new ArrayList<>();

            for (StatementContext child : ctx.statement()) {
                bodyNodes.add(visitStatement(child));
            }

            exitBlock();

            List<SubayaiStatementNode> flattenedNodes = new ArrayList<>(bodyNodes.size());
            flattenBlocks(bodyNodes, flattenedNodes);
            int n = flattenedNodes.size();
            for (int i = 0; i < n; i++) {
                SubayaiStatementNode statement = flattenedNodes.get(i);
                if (statement.hasSource() && !isHaltInCondition(statement)) {
                    statement.addStatementTag();
                }
            }
            SubayaiBlockNode blockNode = new SubayaiBlockNode(flattenedNodes.toArray(new SubayaiStatementNode[flattenedNodes.size()]));
            blockNode.setSourceSection(startPos, endPos - startPos);
            return blockNode;
        }

        private void flattenBlocks(Iterable<? extends SubayaiStatementNode> bodyNodes, List<SubayaiStatementNode> flattenedNodes) {
            for (SubayaiStatementNode n : bodyNodes) {
                if (n instanceof SubayaiBlockNode) {
                    flattenBlocks(((SubayaiBlockNode) n).getStatements(), flattenedNodes);
                } else {
                    flattenedNodes.add(n);
                }
            }
        }

        @Override
        public SubayaiStatementNode visitDebugger_statement(Debugger_statementContext ctx) {
            final SubayaiDebuggerNode debuggerNode = new SubayaiDebuggerNode();
            srcFromToken(debuggerNode, ctx.d);
            return debuggerNode;
        }

        @Override
        public SubayaiStatementNode visitBreak_statement(Break_statementContext ctx) {
            if (loopDepth == 0) {
                semErr(ctx.b, "break used outside of loop");
            }
            final SubayaiBreakNode breakNode = new SubayaiBreakNode();
            srcFromToken(breakNode, ctx.b);
            return breakNode;
        }

        @Override
        public SubayaiStatementNode visitContinue_statement(Continue_statementContext ctx) {
            if (loopDepth == 0) {
                semErr(ctx.c, "continue used outside of loop");
            }
            final SubayaiContinueNode continueNode = new SubayaiContinueNode();
            srcFromToken(continueNode, ctx.c);
            return continueNode;
        }

        @Override
        public SubayaiStatementNode visitWhile_statement(While_statementContext ctx) {
            SubayaiExpressionNode conditionNode = expressionVisitor.visitExpression(ctx.condition);

            loopDepth++;
            SubayaiStatementNode bodyNode = visitBlock(ctx.body);
            loopDepth--;

            conditionNode.addStatementTag();
            final int start = ctx.w.getStartIndex();
            final int end = bodyNode.getSourceEndIndex();
            final SubayaiWhileNode whileNode = new SubayaiWhileNode(conditionNode, bodyNode);
            whileNode.setSourceSection(start, end - start);
            return whileNode;
        }

        @Override
        public SubayaiStatementNode visitIf_statement(If_statementContext ctx) {
            SubayaiExpressionNode conditionNode = expressionVisitor.visitExpression(ctx.condition);
            SubayaiStatementNode thenPartNode = visitBlock(ctx.then);
            SubayaiStatementNode elsePartNode = ctx.alt == null ? null : visitBlock(ctx.alt);

            conditionNode.addStatementTag();
            final int start = ctx.i.getStartIndex();
            final int end = elsePartNode == null ? thenPartNode.getSourceEndIndex() : elsePartNode.getSourceEndIndex();
            final SubayaiIfNode ifNode = new SubayaiIfNode(conditionNode, thenPartNode, elsePartNode);
            ifNode.setSourceSection(start, end - start);
            return ifNode;
        }

        @Override
        public SubayaiStatementNode visitReturn_statement(Return_statementContext ctx) {

            final SubayaiExpressionNode valueNode;
            if (ctx.expression() != null) {
                valueNode = expressionVisitor.visitExpression(ctx.expression());
            } else {
                valueNode = null;
            }

            final int start = ctx.r.getStartIndex();
            final int length = valueNode == null ? ctx.r.getText().length() : valueNode.getSourceEndIndex() - start;
            final SubayaiReturnNode returnNode = new SubayaiReturnNode(valueNode);
            returnNode.setSourceSection(start, length);
            return returnNode;
        }

        @Override
        public SubayaiStatementNode visitStatement(StatementContext ctx) {
            return visit(ctx.getChild(0));
        }

        @Override
        public SubayaiStatementNode visitExpression_statement(Expression_statementContext ctx) {
            return expressionVisitor.visitExpression(ctx.expression());
        }

        @Override
        public SubayaiStatementNode visitChildren(RuleNode arg0) {
            throw new UnsupportedOperationException("node: " + arg0.getClass().getSimpleName());
        }
    }

    private class SubayaiExpressionVisitor extends SubayaiBaseVisitor<SubayaiExpressionNode> {
        @Override
        public SubayaiExpressionNode visitExpression(ExpressionContext ctx) {
            return createBinary(ctx.logic_term(), ctx.OP_OR());
        }

        @Override
        public SubayaiExpressionNode visitLogic_term(Logic_termContext ctx) {
            return createBinary(ctx.logic_factor(), ctx.OP_AND());
        }

        @Override
        public SubayaiExpressionNode visitLogic_factor(Logic_factorContext ctx) {
            return createBinary(ctx.arithmetic(), ctx.OP_COMPARE());
        }

        @Override
        public SubayaiExpressionNode visitArithmetic(ArithmeticContext ctx) {
            return createBinary(ctx.term(), ctx.OP_ADD());
        }

        @Override
        public SubayaiExpressionNode visitTerm(TermContext ctx) {
            return createBinary(ctx.factor(), ctx.OP_MUL());
        }

        private SubayaiExpressionNode createBinary(List<? extends ParserRuleContext> children, TerminalNode op) {
            if (op == null) {
                assert children.size() == 1;
                return visit(children.get(0));
            } else {
                assert children.size() == 2;
                return createBinary(op.getSymbol(), visit(children.get(0)), visit(children.get(1)));
            }
        }

        private SubayaiExpressionNode createBinary(List<? extends ParserRuleContext> children, List<TerminalNode> ops) {
            assert children.size() == ops.size() + 1;

            SubayaiExpressionNode result = visit(children.get(0));

            for (int i = 0; i < ops.size(); i++) {
                result = createBinary(ops.get(i).getSymbol(), result, visit(children.get(i + 1)));
            }

            return result;
        }

        private SubayaiExpressionNode createBinary(Token opToken, SubayaiExpressionNode leftNode, SubayaiExpressionNode rightNode) {
            final SubayaiExpressionNode leftUnboxed = SubayaiUnboxNodeGen.create(leftNode);
            final SubayaiExpressionNode rightUnboxed = SubayaiUnboxNodeGen.create(rightNode);

            final SubayaiExpressionNode result;
            switch (opToken.getText()) {
                case "+":
                    result = SubayaiAddNodeGen.create(leftUnboxed, rightUnboxed);
                    break;
                case "*":
                    result = SubayaiMulNodeGen.create(leftUnboxed, rightUnboxed);
                    break;
                case "/":
                    result = SubayaiDivNodeGen.create(leftUnboxed, rightUnboxed);
                    break;
                case "-":
                    result = SubayaiSubNodeGen.create(leftUnboxed, rightUnboxed);
                    break;
                case "<":
                    result = SubayaiLessThanNodeGen.create(leftUnboxed, rightUnboxed);
                    break;
                case "<=":
                    result = SubayaiLessOrEqualNodeGen.create(leftUnboxed, rightUnboxed);
                    break;
                case ">":
                    result = SubayaiLogicalNotNodeGen.create(SubayaiLessOrEqualNodeGen.create(leftUnboxed, rightUnboxed));
                    break;
                case ">=":
                    result = SubayaiLogicalNotNodeGen.create(SubayaiLessThanNodeGen.create(leftUnboxed, rightUnboxed));
                    break;
                case "==":
                    result = SubayaiEqualNodeGen.create(leftUnboxed, rightUnboxed);
                    break;
                case "!=":
                    result = SubayaiLogicalNotNodeGen.create(SubayaiEqualNodeGen.create(leftUnboxed, rightUnboxed));
                    break;
                case "&&":
                    result = new SubayaiLogicalAndNode(leftUnboxed, rightUnboxed);
                    break;
                case "||":
                    result = new SubayaiLogicalOrNode(leftUnboxed, rightUnboxed);
                    break;
                default:
                    throw new RuntimeException("unexpected operation: " + opToken.getText());
            }

            int start = leftNode.getSourceCharIndex();
            int length = rightNode.getSourceEndIndex() - start;
            result.setSourceSection(start, length);
            result.addExpressionTag();

            return result;
        }

        @Override
        public SubayaiExpressionNode visitNameAccess(NameAccessContext ctx) {

            if (ctx.member_expression().isEmpty()) {
                return createRead(createString(ctx.IDENTIFIER().getSymbol(), false));
            }

            MemberExpressionVisitor visitor = new MemberExpressionVisitor(null, null,
                            createString(ctx.IDENTIFIER().getSymbol(), false));

            for (Member_expressionContext child : ctx.member_expression()) {
                visitor.visit(child);
            }

            return visitor.receiver;
        }

        @Override
        public SubayaiExpressionNode visitStringLiteral(StringLiteralContext ctx) {
            return createString(ctx.STRING_LITERAL().getSymbol(), true);
        }

        @Override
        public SubayaiExpressionNode visitNumericLiteral(NumericLiteralContext ctx) {
            Token literalToken = ctx.NUMERIC_LITERAL().getSymbol();
            SubayaiExpressionNode result;
            try {
                /* Try if the literal is small enough to fit into a long value. */
                result = new SubayaiLongLiteralNode(Long.parseLong(literalToken.getText()));
            } catch (NumberFormatException ex) {
                /* Overflow of long value, so fall back to BigInteger. */
                result = new SubayaiBigIntegerLiteralNode(new BigInteger(literalToken.getText()));
            }
            srcFromToken(result, literalToken);
            result.addExpressionTag();
            return result;
        }

        @Override
        public SubayaiExpressionNode visitParenExpression(ParenExpressionContext ctx) {

            SubayaiExpressionNode expressionNode = visitExpression(ctx.expression());
            if (expressionNode == null) {
                return null;
            }

            int start = ctx.start.getStartIndex();
            int length = ctx.stop.getStopIndex() - start + 1;

            final SubayaiParenExpressionNode result = new SubayaiParenExpressionNode(expressionNode);
            result.setSourceSection(start, length);
            return result;
        }

    }

    private class MemberExpressionVisitor extends SubayaiBaseVisitor<SubayaiExpressionNode> {
        SubayaiExpressionNode receiver;
        private SubayaiExpressionNode assignmentReceiver;
        private SubayaiExpressionNode assignmentName;

        MemberExpressionVisitor(SubayaiExpressionNode r, SubayaiExpressionNode assignmentReceiver, SubayaiExpressionNode assignmentName) {
            this.receiver = r;
            this.assignmentReceiver = assignmentReceiver;
            this.assignmentName = assignmentName;
        }

        @Override
        public SubayaiExpressionNode visitMemberCall(MemberCallContext ctx) {
            List<SubayaiExpressionNode> parameters = new ArrayList<>();
            if (receiver == null) {
                receiver = createRead(assignmentName);
            }

            for (ExpressionContext child : ctx.expression()) {
                parameters.add(expressionVisitor.visitExpression(child));
            }

            final SubayaiExpressionNode result = new SubayaiInvokeNode(receiver, parameters.toArray(new SubayaiExpressionNode[parameters.size()]));

            final int startPos = receiver.getSourceCharIndex();
            final int endPos = ctx.stop.getStopIndex() + 1;
            result.setSourceSection(startPos, endPos - startPos);
            result.addExpressionTag();

            assignmentReceiver = receiver;
            receiver = result;
            assignmentName = null;
            return result;
        }

        @Override
        public SubayaiExpressionNode visitMemberAssign(MemberAssignContext ctx) {
            final SubayaiExpressionNode result;
            if (assignmentName == null) {
                semErr(ctx.expression().start, "invalid assignment target");
                result = null;
            } else if (assignmentReceiver == null) {
                SubayaiExpressionNode valueNode = expressionVisitor.visitExpression(ctx.expression());
                result = createAssignment((SubayaiStringLiteralNode) assignmentName, valueNode, null);
            } else {
                // create write property
                SubayaiExpressionNode valueNode = expressionVisitor.visitExpression(ctx.expression());

                result = SubayaiWritePropertyNodeGen.create(assignmentReceiver, assignmentName, valueNode);

                final int start = assignmentReceiver.getSourceCharIndex();
                final int length = valueNode.getSourceEndIndex() - start;
                result.setSourceSection(start, length);
                result.addExpressionTag();
            }

            assignmentReceiver = receiver;
            receiver = result;
            assignmentName = null;

            return result;
        }

        @Override
        public SubayaiExpressionNode visitMemberField(MemberFieldContext ctx) {
            if (receiver == null) {
                receiver = createRead(assignmentName);
            }

            SubayaiExpressionNode nameNode = createString(ctx.IDENTIFIER().getSymbol(), false);
            assignmentName = nameNode;

            final SubayaiExpressionNode result = SubayaiReadPropertyNodeGen.create(receiver, nameNode);

            final int startPos = receiver.getSourceCharIndex();
            final int endPos = nameNode.getSourceEndIndex();
            result.setSourceSection(startPos, endPos - startPos);
            result.addExpressionTag();

            assignmentReceiver = receiver;
            receiver = result;

            return result;
        }

        @Override
        public SubayaiExpressionNode visitMemberIndex(MemberIndexContext ctx) {
            if (receiver == null) {
                receiver = createRead(assignmentName);
            }

            SubayaiExpressionNode nameNode = expressionVisitor.visitExpression(ctx.expression());
            assignmentName = nameNode;

            final SubayaiExpressionNode result = SubayaiReadPropertyNodeGen.create(receiver, nameNode);

            final int startPos = receiver.getSourceCharIndex();
            final int endPos = nameNode.getSourceEndIndex();
            result.setSourceSection(startPos, endPos - startPos);
            result.addExpressionTag();

            assignmentReceiver = receiver;
            receiver = result;

            return result;
        }

    }

    private SubayaiExpressionNode createRead(SubayaiExpressionNode nameTerm) {
        final TruffleString name = ((SubayaiStringLiteralNode) nameTerm).executeGeneric(null);
        final SubayaiExpressionNode result;
        final int frameSlot = getLocalIndex(name);
        if (frameSlot != -1) {
            result = SubayaiReadLocalVariableNodeGen.create(frameSlot);
        } else {
            result = SubayaiFunctionLiteralNodeGen.create(new SubayaiStringLiteralNode(name));
        }
        result.setSourceSection(nameTerm.getSourceCharIndex(), nameTerm.getSourceLength());
        result.addExpressionTag();
        return result;
    }

    private SubayaiExpressionNode createAssignment(SubayaiStringLiteralNode assignmentName, SubayaiExpressionNode valueNode, Integer index) {

        TruffleString name = assignmentName.executeGeneric(null);

        int frameSlot = getLocalIndex(name);
        assert frameSlot != -1;
        boolean newVariable = initializeLocal(name);
        SubayaiExpressionNode result = SubayaiWriteLocalVariableNodeGen.create(valueNode, frameSlot, assignmentName, newVariable);

        assert index != null || valueNode.hasSource();

        if (valueNode.hasSource()) {
            final int start = assignmentName.getSourceCharIndex();
            final int length = valueNode.getSourceEndIndex() - start;
            result.setSourceSection(start, length);
        }

        if (index == null) {
            result.addExpressionTag();
        }

        return result;
    }

    private static boolean isHaltInCondition(SubayaiStatementNode statement) {
        return (statement instanceof SubayaiIfNode) || (statement instanceof SubayaiWhileNode);
    }

    private static void srcFromToken(SubayaiStatementNode node, Token token) {
        node.setSourceSection(token.getStartIndex(), token.getText().length());
    }

}
