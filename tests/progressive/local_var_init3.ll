%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }

@.Main_vtable = global [0 x i8*] []
@.A_vtable = global [2 x i8*] [
	i8* bitcast (i32 (i8*, i32, i8*)* @A.foo to i8*),
	i8* bitcast (i1 (i8*, %_IntegerArray*, %_BooleanArray*)* @A.bar to i8*)
]

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
	%a = alloca i32
	store i32 0, i32* %a

	%b = alloca i1
	store i1 0, i1* %b

	%c = alloca %_IntegerArray*
	store %_IntegerArray* null, %_IntegerArray** %c

	%d = alloca %_BooleanArray*
	store %_BooleanArray* null, %_BooleanArray** %d

	%e = alloca i8*
	store i8* null, i8** %e


	ret i32 0
}

define i32 @A.foo(i8* %this, i32 %.a, i8* %.b) {
	%a = alloca i32
	store i32 %.a, i32* %a

	%b = alloca i8*
	store i8* %.b, i8** %b


	ret i32 1
}

define i1 @A.bar(i8* %this, %_IntegerArray* %.a, %_BooleanArray* %.b) {
	%a = alloca %_IntegerArray*
	store %_IntegerArray* %.a, %_IntegerArray** %a

	%b = alloca %_BooleanArray*
	store %_BooleanArray* %.b, %_BooleanArray** %b


	ret i1 0
}

