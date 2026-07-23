// Generated from language/src/main/java/space/meowcats/subayai/parser/Subayai.g4 by ANTLR 4.13.2
package space.meowcats.subayai.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link SubayaiParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface SubayaiVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#subayai}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubayai(SubayaiParser.SubayaiContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#function}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunction(SubayaiParser.FunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(SubayaiParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(SubayaiParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#break_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreak_statement(SubayaiParser.Break_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#continue_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinue_statement(SubayaiParser.Continue_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#expression_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression_statement(SubayaiParser.Expression_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#debugger_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDebugger_statement(SubayaiParser.Debugger_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#while_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhile_statement(SubayaiParser.While_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#if_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_statement(SubayaiParser.If_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#return_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturn_statement(SubayaiParser.Return_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(SubayaiParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#logic_term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_term(SubayaiParser.Logic_termContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#logic_factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogic_factor(SubayaiParser.Logic_factorContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#arithmetic}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArithmetic(SubayaiParser.ArithmeticContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(SubayaiParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NameAccess}
	 * labeled alternative in {@link SubayaiParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameAccess(SubayaiParser.NameAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringLiteral}
	 * labeled alternative in {@link SubayaiParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(SubayaiParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumericLiteral}
	 * labeled alternative in {@link SubayaiParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumericLiteral(SubayaiParser.NumericLiteralContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ParenExpression}
	 * labeled alternative in {@link SubayaiParser#factor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenExpression(SubayaiParser.ParenExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MemberCall}
	 * labeled alternative in {@link SubayaiParser#member_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberCall(SubayaiParser.MemberCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MemberAssign}
	 * labeled alternative in {@link SubayaiParser#member_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberAssign(SubayaiParser.MemberAssignContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MemberField}
	 * labeled alternative in {@link SubayaiParser#member_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberField(SubayaiParser.MemberFieldContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MemberIndex}
	 * labeled alternative in {@link SubayaiParser#member_expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberIndex(SubayaiParser.MemberIndexContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#pkg_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPkg_declaration(SubayaiParser.Pkg_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link SubayaiParser#qualified_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQualified_name(SubayaiParser.Qualified_nameContext ctx);
}