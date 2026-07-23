

grammar Subayai;

@parser::header
{
// DO NOT MODIFY - generated from Subayai.g4
}

@lexer::header
{
// DO NOT MODIFY - generated from Subayai.g4
}

// parser 




subayai
	: pkg_declaration
	    use_declaration*
	    function*
	    EOF
	;


function
	: 'fnc' IDENTIFIER
		s='(' (IDENTIFIER (',' IDENTIFIER)*)? ')'
		body=block
	;



block
	: s='{' statement* e='}'
	;


statement
	: while_statement
	| break_statement
	| continue_statement
	| if_statement
	| return_statement
	| expression_statement
	| debugger_statement
	;

break_statement
	: b='break'	';'
	;

continue_statement
	: c='continue' ';'
	;

expression_statement
	: expression ';'
	;

debugger_statement
	: d='debugger' ';'
	;

while_statement
	: w='while' '(' condition=expression ')'
		body=block
	;


if_statement
	: i='if' '(' condition=expression ')'
		then=block 
		( 'else' alt=block )?
	;


return_statement
	: r='return' expression? ';'
	;


expression
	: logic_term (OP_OR logic_term)*
	;


logic_term
	: logic_factor (OP_AND logic_factor)*
	;


logic_factor
	: arithmetic (OP_COMPARE arithmetic)?
	;


arithmetic
	: term (OP_ADD term)*
	;


term
	: factor (OP_MUL factor)*
	;


factor
	: IDENTIFIER member_expression* # NameAccess
	| STRING_LITERAL				# StringLiteral
	| NUMERIC_LITERAL				# NumericLiteral
	| '(' expression ')'			# ParenExpression
	;


member_expression
	: '(' ( expression (',' expression)* )? ')' 	# MemberCall
	| '=' expression							  	# MemberAssign
	| '.' IDENTIFIER						      	# MemberField
	| '[' expression ']'							# MemberIndex
	;

pkg_declaration
    : 'pkg' qualified_name ';'
    ;

use_declaration
    : 'use' qualified_name ';'
    ;

qualified_name
    : IDENTIFIER ('.' IDENTIFIER)*
    ;
// lexer

WS : [ \t\r\n\u000C]+ -> skip;
COMMENT : '/*' .*? '*/' -> skip;
LINE_COMMENT : '//' ~[\r\n]* -> skip;

OP_OR: '||';
OP_AND: '&&';
OP_COMPARE: '<' | '<=' | '>' | '>=' | '==' | '!=';
OP_ADD: '+' | '-';
OP_MUL: '*' | '/';

fragment LETTER : [A-Z] | [a-z] | '_' | '$';
fragment NON_ZERO_DIGIT : [1-9];
fragment DIGIT : [0-9];
fragment HEX_DIGIT : [0-9] | [a-f] | [A-F];
fragment OCT_DIGIT : [0-7];
fragment BINARY_DIGIT : '0' | '1';
fragment TAB : '\t';
fragment STRING_CHAR : ~('"' | '\r' | '\n');

IDENTIFIER : LETTER (LETTER | DIGIT)*;
STRING_LITERAL : '"' STRING_CHAR* '"';
NUMERIC_LITERAL : '0' | NON_ZERO_DIGIT DIGIT*;

