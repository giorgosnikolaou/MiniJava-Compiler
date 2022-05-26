%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }

@.BrackeExpressionTest_vtable = global [0 x i8*] []
@.A_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*, i32, i32)* @A.foo to i8*)]

declare i8* @calloc(i32, i32)
declare i32 @printf(i8*, ...)
declare void @exit(i32)

@_cint = constant [4 x i8] c"%d\0a\00"
@_cOOB = constant [15 x i8] c"Out of bounds\0a\00"

define void @print_int(i32 %i) {
	%_str = bitcast [4 x i8]* @_cint to i8*
	call i32 (i8*, ...) @printf(i8* %_str, i32 %i)
	ret void
}

define void @throw_oob() {
	%_str = bitcast [15 x i8]* @_cOOB to i8*
	call i32 (i8*, ...) @printf(i8* %_str)
	call void @exit(i32 1)
	ret void
}

define i32 @main() {
	%n = alloca i32
	store i32 0, i32* %n

	%_0 = call i8* @calloc(i32 1, i32 8)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.A_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1

	%_3 = bitcast i8* %_0 to i8***
	%_4 = load i8**, i8*** %_3
	%_5 = getelementptr i8*, i8** %_4, i32 0
	%_6 = load i8*, i8** %_5
	%_7 = bitcast i8* %_6 to i32 (i8*, i32, i32)*
	%_8 = call i32 %_7(i8* %_0,i32 10, i32 15)
	store i32 %_8, i32* %n

	%_9 = load i32, i32* %n
	call void (i32) @print_int(i32  %_9)

	%_10 = add i32 1, 23

	store i32 %_10, i32* %n

	%_11 = load i32, i32* %n
	call void (i32) @print_int(i32  %_11)


	ret i32 0
}

define i32 @A.foo(i8* %this, i32 %.i, i32 %.j) {
	%i = alloca i32
	store i32 %.i, i32* %i

	%j = alloca i32
	store i32 %.j, i32* %j

	%_12 = load i32, i32* %i
	%_13 = load i32, i32* %j
	%_14 = add i32 %_12, %_13


	ret i32 %_14
}

