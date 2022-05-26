%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }

@.InheritanceTest_vtable = global [0 x i8*] []
@.A_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*, i32)* @A.foo to i8*)]
@.B_vtable = global [1 x i8*] [i8* bitcast (i32 (i8*, i32)* @B.foo to i8*)]

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

	%A = alloca i8*
	store i8* null, i8** %A

	%_0 = call i8* @calloc(i32 1, i32 16)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [1 x i8*], [1 x i8*]* @.B_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1

	store i8* %_0, i8** %A

	%_3 = load i8*, i8** %A
	%_4 = bitcast i8* %_3 to i8***
	%_5 = load i8**, i8*** %_4
	%_6 = getelementptr i8*, i8** %_5, i32 0
	%_7 = load i8*, i8** %_6
	%_8 = bitcast i8* %_7 to i32 (i8*, i32)*
	%_9 = call i32 %_8(i8* %_3,i32 2)
	store i32 %_9, i32* %n


	ret i32 0
}

define i32 @A.foo(i8* %this, i32 %.i) {
	%i = alloca i32
	store i32 %.i, i32* %i

	%_10 = load i32, i32* %i
	%_11 = add i32 %_10, 5


	ret i32 %_11
}

define i32 @B.foo(i8* %this, i32 %.i) {
	%i = alloca i32
	store i32 %.i, i32* %i

	%_12 = load i32, i32* %i
	%_13 = getelementptr i8, i8* %this, i32 12
	%_14 = bitcast i8* %_13 to i32*
	%_15 = load i32, i32* %_14

	%_16 = add i32 %_12, %_15


	ret i32 %_16
}

