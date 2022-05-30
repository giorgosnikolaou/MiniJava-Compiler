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
	%a = alloca %_IntegerArray*
	store %_IntegerArray* null, %_IntegerArray** %a

	%i = alloca i32
	store i32 0, i32* %i

	%_0 = icmp sge i32  10, 0

	br i1 %_0, label %L1, label %L0

L0:
	
	call void @throw_oob()
	br label %L1

L1:
	%_1 = call i8* @calloc(i32 1, i32 12)
	%_2 = bitcast i8* %_1 to %_IntegerArray*
	%_3 = getelementptr %_IntegerArray, %_IntegerArray* %_2, i32 0, i32 0
	store i32  10, i32* %_3
	%_4 = call i8* @calloc(i32 4, i32  10)
	%_5 = bitcast i8* %_4 to i32*
	%_6 = getelementptr %_IntegerArray, %_IntegerArray* %_2, i32 0, i32 1
	store i32* %_5, i32** %_6
	store %_IntegerArray* %_2, %_IntegerArray** %a
	
	store i32 0, i32* %i
	
	br label %L2

L2:
	%_7 = load i32, i32* %i
	%_8 = load %_IntegerArray*, %_IntegerArray** %a
	%_9 = getelementptr %_IntegerArray, %_IntegerArray* %_8, i32 0, i32 0
	%_10 = load i32, i32* %_9
	%_11 = icmp slt i32 %_7, %_10

	br i1 %_11, label %L3, label %L4

L3:
	%_12 = load %_IntegerArray*, %_IntegerArray** %a
	%_13 = load i32, i32* %i
	%_14 = icmp sge i32 %_13, 0

	%_15 = getelementptr %_IntegerArray, %_IntegerArray* %_12, i32 0, i32 0
	%_16 = load i32, i32* %_15
	%_17 = icmp slt i32 %_13, %_16

	%_18 = and i1 %_14, %_17

	br i1 %_18, label %L6, label %L5

L5:
	
	call void @throw_oob()
	br label %L6

L6:
	%_19 = getelementptr %_IntegerArray, %_IntegerArray* %_12, i32 0, i32 1
	%_20 = load i32*, i32** %_19
	%_21 = getelementptr i32, i32* %_20, i32 %_13
	%_22 = load i32, i32* %i
	store i32 %_22, i32* %_21
	
	%_23 = load i32, i32* %i
	%_24 = add i32 %_23, 1

	store i32 %_24, i32* %i
	
	br label %L2

L4:
	
	store i32 0, i32* %i
	
	br label %L7

L7:
	%_25 = load i32, i32* %i
	%_26 = load %_IntegerArray*, %_IntegerArray** %a
	%_27 = getelementptr %_IntegerArray, %_IntegerArray* %_26, i32 0, i32 0
	%_28 = load i32, i32* %_27
	%_29 = icmp slt i32 %_25, %_28

	br i1 %_29, label %L8, label %L9

L8:
	%_30 = load %_IntegerArray*, %_IntegerArray** %a
	%_31 = load i32, i32* %i
	%_32 = icmp sge i32 %_31, 0

	%_33 = getelementptr %_IntegerArray, %_IntegerArray* %_30, i32 0, i32 0
	%_34 = load i32, i32* %_33
	%_35 = icmp slt i32 %_31, %_34

	%_36 = and i1 %_32, %_35

	br i1 %_36, label %L11, label %L10

L10:
	
	call void @throw_oob()
	br label %L11

L11:
	%_37 = getelementptr %_IntegerArray, %_IntegerArray* %_30, i32 0, i32 1
	%_38 = load i32*, i32** %_37
	%_39 = getelementptr i32, i32* %_38, i32 %_31
	%_40 = load i32, i32* %_39
	call void (i32) @print_int(i32  %_40)

	%_41 = load i32, i32* %i
	%_42 = add i32 %_41, 1

	store i32 %_42, i32* %i
	
	br label %L7

L9:
	
	%_43 = load %_IntegerArray*, %_IntegerArray** %a
	%_44 = icmp sge i32 2, 0

	%_45 = getelementptr %_IntegerArray, %_IntegerArray* %_43, i32 0, i32 0
	%_46 = load i32, i32* %_45
	%_47 = icmp slt i32 2, %_46

	%_48 = and i1 %_44, %_47

	br i1 %_48, label %L13, label %L12

L12:
	
	call void @throw_oob()
	br label %L13

L13:
	%_49 = getelementptr %_IntegerArray, %_IntegerArray* %_43, i32 0, i32 1
	%_50 = load i32*, i32** %_49
	%_51 = getelementptr i32, i32* %_50, i32 2
	store i32 10, i32* %_51
	
	%_52 = load %_IntegerArray*, %_IntegerArray** %a
	%_53 = icmp sge i32 3, 0

	%_54 = getelementptr %_IntegerArray, %_IntegerArray* %_52, i32 0, i32 0
	%_55 = load i32, i32* %_54
	%_56 = icmp slt i32 3, %_55

	%_57 = and i1 %_53, %_56

	br i1 %_57, label %L15, label %L14

L14:
	
	call void @throw_oob()
	br label %L15

L15:
	%_58 = getelementptr %_IntegerArray, %_IntegerArray* %_52, i32 0, i32 1
	%_59 = load i32*, i32** %_58
	%_60 = getelementptr i32, i32* %_59, i32 3
	store i32 5, i32* %_60
	
	%_61 = load %_IntegerArray*, %_IntegerArray** %a
	%_62 = icmp sge i32 2, 0

	%_63 = getelementptr %_IntegerArray, %_IntegerArray* %_61, i32 0, i32 0
	%_64 = load i32, i32* %_63
	%_65 = icmp slt i32 2, %_64

	%_66 = and i1 %_62, %_65

	br i1 %_66, label %L17, label %L16

L16:
	
	call void @throw_oob()
	br label %L17

L17:
	%_67 = getelementptr %_IntegerArray, %_IntegerArray* %_61, i32 0, i32 1
	%_68 = load i32*, i32** %_67
	%_69 = getelementptr i32, i32* %_68, i32 2
	%_70 = load i32, i32* %_69
	%_71 = icmp slt i32 %_70, 1

	br i1 %_71, label %L18, label %L19

L19:
	br label %L20

L18:
	%_72 = load %_IntegerArray*, %_IntegerArray** %a
	%_73 = icmp sge i32 3, 0

	%_74 = getelementptr %_IntegerArray, %_IntegerArray* %_72, i32 0, i32 0
	%_75 = load i32, i32* %_74
	%_76 = icmp slt i32 3, %_75

	%_77 = and i1 %_73, %_76

	br i1 %_77, label %L22, label %L21

L21:
	
	call void @throw_oob()
	br label %L22

L22:
	%_78 = getelementptr %_IntegerArray, %_IntegerArray* %_72, i32 0, i32 1
	%_79 = load i32*, i32** %_78
	%_80 = getelementptr i32, i32* %_79, i32 3
	%_81 = load i32, i32* %_80
	%_82 = icmp slt i32 %_81, 1

	br label %L20

L20:
	%_83 = phi i1 [ %_82, %L22 ], [ 0, %L19 ]
	br i1 %_83, label %L23, label %L24

L23:
	call void (i32) @print_int(i32  1)

	br label %L25

L24:
	%_84 = load %_IntegerArray*, %_IntegerArray** %a
	%_85 = icmp sge i32 2, 0

	%_86 = getelementptr %_IntegerArray, %_IntegerArray* %_84, i32 0, i32 0
	%_87 = load i32, i32* %_86
	%_88 = icmp slt i32 2, %_87

	%_89 = and i1 %_85, %_88

	br i1 %_89, label %L27, label %L26

L26:
	
	call void @throw_oob()
	br label %L27

L27:
	%_90 = getelementptr %_IntegerArray, %_IntegerArray* %_84, i32 0, i32 1
	%_91 = load i32*, i32** %_90
	%_92 = getelementptr i32, i32* %_91, i32 2
	%_93 = load i32, i32* %_92
	call void (i32) @print_int(i32  %_93)

	br label %L25

L25:
	

	ret i32 0
}

