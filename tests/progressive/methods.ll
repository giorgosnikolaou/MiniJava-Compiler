%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }

@.Main_vtable = global [0 x i8*] []
@.A_vtable = global [5 x i8*] [
	i8* bitcast (i32 (i8*, i32, i8*)* @A.foo to i8*),
	i8* bitcast (i1 (i8*, %_IntegerArray*, %_BooleanArray*)* @A.bar to i8*),
	i8* bitcast (i32 (i8*)* @A.get_a to i8*),
	i8* bitcast (i32 (i8*)* @A.get_b to i8*),
	i8* bitcast (i1 (i8*)* @A.get_bool to i8*)
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
	%e = alloca i8*
	store i8* null, i8** %e

	%a = alloca i32
	store i32 0, i32* %a

	%_0 = call i8* @calloc(i32 1, i32 17)
	%_1 = bitcast i8* %_0 to i8***
	%_2 = getelementptr [5 x i8*], [5 x i8*]* @.A_vtable, i32 0, i32 0
	store i8** %_2, i8*** %_1

	store i8** %_2, i8*** %_1
	store i8* %_0, i8** %e
	
	%_3 = load i8*, i8** %e
	%_4 = icmp sge i32  10, 0

	br i1 %_4, label %L1, label %L0

L0:
	
	call void @throw_oob()
	br label %L1

L1:
	%_5 = call i8* @calloc(i32 1, i32 12)
	%_6 = bitcast i8* %_5 to %_IntegerArray*
	%_7 = getelementptr %_IntegerArray, %_IntegerArray* %_6, i32 0, i32 0
	store i32  10, i32* %_7
	%_8 = call i8* @calloc(i32 4, i32  10)
	%_9 = bitcast i8* %_8 to i32*
	%_10 = getelementptr %_IntegerArray, %_IntegerArray* %_6, i32 0, i32 1
	store i32* %_9, i32** %_10
	%_11 = icmp sge i32  20, 0

	br i1 %_11, label %L3, label %L2

L2:
	
	call void @throw_oob()
	br label %L3

L3:
	%_12 = call i8* @calloc(i32 1, i32 12)
	%_13 = bitcast i8* %_12 to %_BooleanArray*
	%_14 = getelementptr %_BooleanArray, %_BooleanArray* %_13, i32 0, i32 0
	store i32  20, i32* %_14
	%_15 = call i8* @calloc(i32 4, i32  20)
	%_16 = bitcast i8* %_15 to i1*
	%_17 = getelementptr %_BooleanArray, %_BooleanArray* %_13, i32 0, i32 1
	store i1* %_16, i1** %_17
	%_18 = bitcast i8* %_3 to i8***
	%_19 = load i8**, i8*** %_18
	%_20 = getelementptr i8*, i8** %_19, i32 1
	%_21 = load i8*, i8** %_20
	%_22 = bitcast i8* %_21 to i1 (i8*, %_IntegerArray*, %_BooleanArray*)*
	%_23 = call i1 %_22(i8* %_3,%_IntegerArray* %_6, %_BooleanArray* %_13)
	br i1 %_23, label %L4, label %L5

L4:
	%_24 = load i8*, i8** %e
	%_25 = load i8*, i8** %e
	%_26 = bitcast i8* %_24 to i8***
	%_27 = load i8**, i8*** %_26
	%_28 = getelementptr i8*, i8** %_27, i32 0
	%_29 = load i8*, i8** %_28
	%_30 = bitcast i8* %_29 to i32 (i8*, i32, i8*)*
	%_31 = call i32 %_30(i8* %_24,i32 10, i8* %_25)
	call void (i32) @print_int(i32  %_31)

	br label %L6

L5:
	call void (i32) @print_int(i32  0)

	br label %L6

L6:
	
	%_32 = load i8*, i8** %e
	%_33 = bitcast i8* %_32 to i8***
	%_34 = load i8**, i8*** %_33
	%_35 = getelementptr i8*, i8** %_34, i32 2
	%_36 = load i8*, i8** %_35
	%_37 = bitcast i8* %_36 to i32 (i8*)*
	%_38 = call i32 %_37(i8* %_32)
	call void (i32) @print_int(i32  %_38)

	%_39 = load i8*, i8** %e
	%_40 = bitcast i8* %_39 to i8***
	%_41 = load i8**, i8*** %_40
	%_42 = getelementptr i8*, i8** %_41, i32 3
	%_43 = load i8*, i8** %_42
	%_44 = bitcast i8* %_43 to i32 (i8*)*
	%_45 = call i32 %_44(i8* %_39)
	call void (i32) @print_int(i32  %_45)

	%_46 = load i8*, i8** %e
	%_47 = bitcast i8* %_46 to i8***
	%_48 = load i8**, i8*** %_47
	%_49 = getelementptr i8*, i8** %_48, i32 4
	%_50 = load i8*, i8** %_49
	%_51 = bitcast i8* %_50 to i1 (i8*)*
	%_52 = call i1 %_51(i8* %_46)
	br i1 %_52, label %L7, label %L8

L7:
	call void (i32) @print_int(i32  1)

	br label %L9

L8:
	call void (i32) @print_int(i32  0)

	br label %L9

L9:
	

	ret i32 0
}

define i32 @A.foo(i8* %this, i32 %.c, i8* %.d) {
	%c = alloca i32
	store i32 %.c, i32* %c

	%d = alloca i8*
	store i8* %.d, i8** %d

	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %c
	store i32 %_2, i32* %_1
	
	%_3 = getelementptr i8, i8* %this, i32 16
	%_4 = bitcast i8* %_3 to i1*
	%_5 = load i8*, i8** %d
	%_6 = getelementptr i8, i8* %this, i32 8
	%_7 = bitcast i8* %_6 to i32*
	%_8 = load i32, i32* %_7
	%_9 = icmp sge i32  %_8, 0

	br i1 %_9, label %L11, label %L10

L10:
	
	call void @throw_oob()
	br label %L11

L11:
	%_10 = call i8* @calloc(i32 1, i32 12)
	%_11 = bitcast i8* %_10 to %_IntegerArray*
	%_12 = getelementptr %_IntegerArray, %_IntegerArray* %_11, i32 0, i32 0
	store i32  %_8, i32* %_12
	%_13 = call i8* @calloc(i32 4, i32  %_8)
	%_14 = bitcast i8* %_13 to i32*
	%_15 = getelementptr %_IntegerArray, %_IntegerArray* %_11, i32 0, i32 1
	store i32* %_14, i32** %_15
	%_16 = icmp sge i32  20, 0

	br i1 %_16, label %L13, label %L12

L12:
	
	call void @throw_oob()
	br label %L13

L13:
	%_17 = call i8* @calloc(i32 1, i32 12)
	%_18 = bitcast i8* %_17 to %_BooleanArray*
	%_19 = getelementptr %_BooleanArray, %_BooleanArray* %_18, i32 0, i32 0
	store i32  20, i32* %_19
	%_20 = call i8* @calloc(i32 4, i32  20)
	%_21 = bitcast i8* %_20 to i1*
	%_22 = getelementptr %_BooleanArray, %_BooleanArray* %_18, i32 0, i32 1
	store i1* %_21, i1** %_22
	%_23 = bitcast i8* %_5 to i8***
	%_24 = load i8**, i8*** %_23
	%_25 = getelementptr i8*, i8** %_24, i32 1
	%_26 = load i8*, i8** %_25
	%_27 = bitcast i8* %_26 to i1 (i8*, %_IntegerArray*, %_BooleanArray*)*
	%_28 = call i1 %_27(i8* %_5,%_IntegerArray* %_11, %_BooleanArray* %_18)
	store i1 %_28, i1* %_4
	
	%_29 = getelementptr i8, i8* %this, i32 12
	%_30 = bitcast i8* %_29 to i32*
	%_31 = load i32, i32* %_30

	ret i32 %_31
}

define i1 @A.bar(i8* %this, %_IntegerArray* %.a, %_BooleanArray* %.c) {
	%a = alloca %_IntegerArray*
	store %_IntegerArray* %.a, %_IntegerArray** %a

	%c = alloca %_BooleanArray*
	store %_BooleanArray* %.c, %_BooleanArray** %c

	%_0 = getelementptr i8, i8* %this, i32 12
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load %_IntegerArray*, %_IntegerArray** %a
	%_3 = getelementptr %_IntegerArray, %_IntegerArray* %_2, i32 0, i32 0
	%_4 = load i32, i32* %_3
	%_5 = load %_BooleanArray*, %_BooleanArray** %c
	%_6 = getelementptr %_BooleanArray, %_BooleanArray* %_5, i32 0, i32 0
	%_7 = load i32, i32* %_6
	%_8 = add i32 %_4, %_7

	store i32 %_8, i32* %_1
	

	ret i1 1
}

define i32 @A.get_a(i8* %this) {
	%_0 = getelementptr i8, i8* %this, i32 8
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1

	ret i32 %_2
}

define i32 @A.get_b(i8* %this) {
	%_0 = getelementptr i8, i8* %this, i32 12
	%_1 = bitcast i8* %_0 to i32*
	%_2 = load i32, i32* %_1

	ret i32 %_2
}

define i1 @A.get_bool(i8* %this) {
	%_0 = getelementptr i8, i8* %this, i32 16
	%_1 = bitcast i8* %_0 to i1*
	%_2 = load i1, i1* %_1

	ret i1 %_2
}

