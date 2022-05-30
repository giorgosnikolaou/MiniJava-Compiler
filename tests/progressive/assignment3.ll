%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }

@.Main_vtable = global [0 x i8*] []
@.A_vtable = global [1 x i8*] [i8* bitcast (%_IntegerArray* (i8*, %_BooleanArray*)* @A.foo to i8*)]

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

	%_0 = sub i32 1, 2

	%_1 = add i32 %_0, 3

	%_2 = sub i32 %_1, 4

	%_3 = mul i32 %_2, 2

	store i32 %_3, i32* %a
	
	%_4 = load i32, i32* %a
	call void (i32) @print_int(i32  %_4)

	store i1 1, i1* %b
	
	%_5 = icmp sge i32  10, 0

	br i1 %_5, label %L1, label %L0

L0:
	
	call void @throw_oob()
	br label %L1

L1:
	%_6 = call i8* @calloc(i32 1, i32 12)
	%_7 = bitcast i8* %_6 to %_IntegerArray*
	%_8 = getelementptr %_IntegerArray, %_IntegerArray* %_7, i32 0, i32 0
	store i32  10, i32* %_8
	%_9 = call i8* @calloc(i32 4, i32  10)
	%_10 = bitcast i8* %_9 to i32*
	%_11 = getelementptr %_IntegerArray, %_IntegerArray* %_7, i32 0, i32 1
	store i32* %_10, i32** %_11
	store %_IntegerArray* %_7, %_IntegerArray** %c
	
	%_12 = call i8* @calloc(i32 1, i32 16)
	%_13 = bitcast i8* %_12 to i8***
	%_14 = getelementptr [1 x i8*], [1 x i8*]* @.A_vtable, i32 0, i32 0
	store i8** %_14, i8*** %_13

	store i8** %_14, i8*** %_13
	store i8* %_12, i8** %e
	
	%_15 = load i32, i32* %a
	%_16 = icmp sge i32  %_15, 0

	br i1 %_16, label %L3, label %L2

L2:
	
	call void @throw_oob()
	br label %L3

L3:
	%_17 = call i8* @calloc(i32 1, i32 12)
	%_18 = bitcast i8* %_17 to %_BooleanArray*
	%_19 = getelementptr %_BooleanArray, %_BooleanArray* %_18, i32 0, i32 0
	store i32  %_15, i32* %_19
	%_20 = call i8* @calloc(i32 4, i32  %_15)
	%_21 = bitcast i8* %_20 to i1*
	%_22 = getelementptr %_BooleanArray, %_BooleanArray* %_18, i32 0, i32 1
	store i1* %_21, i1** %_22
	store %_BooleanArray* %_18, %_BooleanArray** %d
	

	ret i32 0
}

define %_IntegerArray* @A.foo(i8* %this, %_BooleanArray* %.a) {
	%a = alloca %_BooleanArray*
	store %_BooleanArray* %.a, %_BooleanArray** %a

	%b = alloca %_IntegerArray*
	store %_IntegerArray* null, %_IntegerArray** %b

	%_0 = load %_BooleanArray*, %_BooleanArray** %a
	%_1 = getelementptr %_BooleanArray, %_BooleanArray* %_0, i32 0, i32 0
	%_2 = load i32, i32* %_1
	%_3 = icmp sge i32  %_2, 0

	br i1 %_3, label %L5, label %L4

L4:
	
	call void @throw_oob()
	br label %L5

L5:
	%_4 = call i8* @calloc(i32 1, i32 12)
	%_5 = bitcast i8* %_4 to %_IntegerArray*
	%_6 = getelementptr %_IntegerArray, %_IntegerArray* %_5, i32 0, i32 0
	store i32  %_2, i32* %_6
	%_7 = call i8* @calloc(i32 4, i32  %_2)
	%_8 = bitcast i8* %_7 to i32*
	%_9 = getelementptr %_IntegerArray, %_IntegerArray* %_5, i32 0, i32 1
	store i32* %_8, i32** %_9
	store %_IntegerArray* %_5, %_IntegerArray** %b
	
	%_10 = load %_IntegerArray*, %_IntegerArray** %b

	ret %_IntegerArray* %_10
}

