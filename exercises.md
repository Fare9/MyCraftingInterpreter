# Chapter 5: Representing Code

1. Take this grammar:

```
expr -> expr ( "(" ( expr ( "," expr )* )? ")" | "." IDENTIFIER)+
	| IDENTIFIER
	| NUMBER
```

Produce a grammar that matches the same language but does not use any notational sugar like |, * and +.

```
expr -> expr_with_dots
expr_with_dots -> expr_with_dots "(" expr_comma_expr_int ")"
expr_with_dots -> expr_with_dots dot_ident
expr_with_dots -> IDENTIFIER
expr_with_dots -> NUMBER
expr_comma_expr_int -> expr_comma_expr?
expr_comma_expr -> expr comma_expr_rec_int
comma_expr_rec_int -> comma_expr_rec ?
comma_expr_rec -> comma_expr comma_expr_rec
comma_expr -> "," expr
dot_ident -> "." IDENTIFIER
```




