%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }

@.Main_vtable = global [0 x i8*] []
@.A_vtable = global [0 x i8*] []

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

	%_0 = sub i32 0, 1

	store i32 %_0, i32* %a
	
	store i1 1, i1* %b
	
	%_1 = icmp sge i32  10, 0

	br i1 %_1, label %L1, label %L0

L0:
	
	call void @throw_oob()
	br label %L1

L1:
	%_2 = call i8* @calloc(i32 1, i32 12)
	%_3 = bitcast i8* %_2 to %_IntegerArray*
	%_4 = getelementptr %_IntegerArray, %_IntegerArray* %_3, i32 0, i32 0
	store i32  10, i32* %_4
	%_5 = call i8* @calloc(i32 4, i32  10)
	%_6 = bitcast i8* %_5 to i32*
	%_7 = getelementptr %_IntegerArray, %_IntegerArray* %_3, i32 0, i32 1
	store i32* %_6, i32** %_7
	store %_IntegerArray* %_3, %_IntegerArray** %c
	
	%_8 = icmp sge i32  11, 0

	br i1 %_8, label %L3, label %L2

L2:
	
	call void @throw_oob()
	br label %L3

L3:
	%_9 = call i8* @calloc(i32 1, i32 12)
	%_10 = bitcast i8* %_9 to %_BooleanArray*
	%_11 = getelementptr %_BooleanArray, %_BooleanArray* %_10, i32 0, i32 0
	store i32  11, i32* %_11
	%_12 = call i8* @calloc(i32 4, i32  11)
	%_13 = bitcast i8* %_12 to i1*
	%_14 = getelementptr %_BooleanArray, %_BooleanArray* %_10, i32 0, i32 1
	store i1* %_13, i1** %_14
	store %_BooleanArray* %_10, %_BooleanArray** %d
	
	%_15 = call i8* @calloc(i32 1, i32 8)
	%_16 = bitcast i8* %_15 to i8***
	%_17 = getelementptr [0 x i8*], [0 x i8*]* @.A_vtable, i32 0, i32 0
	store i8** %_17, i8*** %_16

	store i8** %_17, i8*** %_16
	store i8* %_15, i8** %e
	

	ret i32 0
}

