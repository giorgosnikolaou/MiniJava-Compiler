%_BooleanArray = type { i32, i1* }
%_IntegerArray = type { i32, i32* }

@.Main_vtable = global [0 x i8*] []

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
	
	%_12 = load i32, i32* %a
	%_13 = add i32 %_12, 10

	%_14 = icmp sge i32  %_13, 0

	br i1 %_14, label %L3, label %L2

L2:
	
	call void @throw_oob()
	br label %L3

L3:
	%_15 = call i8* @calloc(i32 1, i32 12)
	%_16 = bitcast i8* %_15 to %_BooleanArray*
	%_17 = getelementptr %_BooleanArray, %_BooleanArray* %_16, i32 0, i32 0
	store i32  %_13, i32* %_17
	%_18 = call i8* @calloc(i32 4, i32  %_13)
	%_19 = bitcast i8* %_18 to i1*
	%_20 = getelementptr %_BooleanArray, %_BooleanArray* %_16, i32 0, i32 1
	store i1* %_19, i1** %_20
	store %_BooleanArray* %_16, %_BooleanArray** %d
	
	%_21 = load %_IntegerArray*, %_IntegerArray** %c
	%_22 = icmp sge i32 1, 0

	%_23 = getelementptr %_IntegerArray, %_IntegerArray* %_21, i32 0, i32 0
	%_24 = load i32, i32* %_23
	%_25 = icmp slt i32 1, %_24

	%_26 = and i1 %_22, %_25

	br i1 %_26, label %L5, label %L4

L4:
	
	call void @throw_oob()
	br label %L5

L5:
	%_27 = getelementptr %_IntegerArray, %_IntegerArray* %_21, i32 0, i32 1
	%_28 = load i32*, i32** %_27
	%_29 = getelementptr i32, i32* %_28, i32 1
	store i32 6, i32* %_29
	
	%_30 = load %_IntegerArray*, %_IntegerArray** %c
	%_31 = load i32, i32* %a
	%_32 = add i32 %_31, 11

	%_33 = icmp sge i32 %_32, 0

	%_34 = getelementptr %_IntegerArray, %_IntegerArray* %_30, i32 0, i32 0
	%_35 = load i32, i32* %_34
	%_36 = icmp slt i32 %_32, %_35

	%_37 = and i1 %_33, %_36

	br i1 %_37, label %L7, label %L6

L6:
	
	call void @throw_oob()
	br label %L7

L7:
	%_38 = getelementptr %_IntegerArray, %_IntegerArray* %_30, i32 0, i32 1
	%_39 = load i32*, i32** %_38
	%_40 = getelementptr i32, i32* %_39, i32 %_32
	%_41 = load %_IntegerArray*, %_IntegerArray** %c
	%_42 = icmp sge i32 1, 0

	%_43 = getelementptr %_IntegerArray, %_IntegerArray* %_41, i32 0, i32 0
	%_44 = load i32, i32* %_43
	%_45 = icmp slt i32 1, %_44

	%_46 = and i1 %_42, %_45

	br i1 %_46, label %L9, label %L8

L8:
	
	call void @throw_oob()
	br label %L9

L9:
	%_47 = getelementptr %_IntegerArray, %_IntegerArray* %_41, i32 0, i32 1
	%_48 = load i32*, i32** %_47
	%_49 = getelementptr i32, i32* %_48, i32 1
	%_50 = load i32, i32* %_49
	store i32 %_50, i32* %_40
	
	%_51 = load %_IntegerArray*, %_IntegerArray** %c
	%_52 = load %_IntegerArray*, %_IntegerArray** %c
	%_53 = load i32, i32* %a
	%_54 = add i32 %_53, 11

	%_55 = icmp sge i32 %_54, 0

	%_56 = getelementptr %_IntegerArray, %_IntegerArray* %_52, i32 0, i32 0
	%_57 = load i32, i32* %_56
	%_58 = icmp slt i32 %_54, %_57

	%_59 = and i1 %_55, %_58

	br i1 %_59, label %L11, label %L10

L10:
	
	call void @throw_oob()
	br label %L11

L11:
	%_60 = getelementptr %_IntegerArray, %_IntegerArray* %_52, i32 0, i32 1
	%_61 = load i32*, i32** %_60
	%_62 = getelementptr i32, i32* %_61, i32 %_54
	%_63 = load i32, i32* %_62
	%_64 = icmp sge i32 %_63, 0

	%_65 = getelementptr %_IntegerArray, %_IntegerArray* %_51, i32 0, i32 0
	%_66 = load i32, i32* %_65
	%_67 = icmp slt i32 %_63, %_66

	%_68 = and i1 %_64, %_67

	br i1 %_68, label %L13, label %L12

L12:
	
	call void @throw_oob()
	br label %L13

L13:
	%_69 = getelementptr %_IntegerArray, %_IntegerArray* %_51, i32 0, i32 1
	%_70 = load i32*, i32** %_69
	%_71 = getelementptr i32, i32* %_70, i32 %_63
	store i32 101, i32* %_71
	
	%_72 = load %_IntegerArray*, %_IntegerArray** %c
	%_73 = icmp sge i32 1, 0

	%_74 = getelementptr %_IntegerArray, %_IntegerArray* %_72, i32 0, i32 0
	%_75 = load i32, i32* %_74
	%_76 = icmp slt i32 1, %_75

	%_77 = and i1 %_73, %_76

	br i1 %_77, label %L15, label %L14

L14:
	
	call void @throw_oob()
	br label %L15

L15:
	%_78 = getelementptr %_IntegerArray, %_IntegerArray* %_72, i32 0, i32 1
	%_79 = load i32*, i32** %_78
	%_80 = getelementptr i32, i32* %_79, i32 1
	%_81 = load i32, i32* %_80
	call void (i32) @print_int(i32  %_81)

	%_82 = load %_IntegerArray*, %_IntegerArray** %c
	%_83 = icmp sge i32 6, 0

	%_84 = getelementptr %_IntegerArray, %_IntegerArray* %_82, i32 0, i32 0
	%_85 = load i32, i32* %_84
	%_86 = icmp slt i32 6, %_85

	%_87 = and i1 %_83, %_86

	br i1 %_87, label %L17, label %L16

L16:
	
	call void @throw_oob()
	br label %L17

L17:
	%_88 = getelementptr %_IntegerArray, %_IntegerArray* %_82, i32 0, i32 1
	%_89 = load i32*, i32** %_88
	%_90 = getelementptr i32, i32* %_89, i32 6
	%_91 = load i32, i32* %_90
	call void (i32) @print_int(i32  %_91)

	%_92 = load %_IntegerArray*, %_IntegerArray** %c
	%_93 = icmp sge i32 7, 0

	%_94 = getelementptr %_IntegerArray, %_IntegerArray* %_92, i32 0, i32 0
	%_95 = load i32, i32* %_94
	%_96 = icmp slt i32 7, %_95

	%_97 = and i1 %_93, %_96

	br i1 %_97, label %L19, label %L18

L18:
	
	call void @throw_oob()
	br label %L19

L19:
	%_98 = getelementptr %_IntegerArray, %_IntegerArray* %_92, i32 0, i32 1
	%_99 = load i32*, i32** %_98
	%_100 = getelementptr i32, i32* %_99, i32 7
	%_101 = load i32, i32* %_100
	call void (i32) @print_int(i32  %_101)


	ret i32 0
}

