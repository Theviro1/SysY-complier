# 错误处理
## 前言
真心推荐最后再写错误处理，其一是经过中间代码生成之后能明白很多东西，在错误处理的时候也可以使用代码生成里的相同的继承属性、综合属性等思想；其二是在代码生成里建的符号表对于错误处理来说是向下兼容的，反之先写错误处理建立的符号表有可能没法在代码生成里使用；其三是可以在最后复习一下词法分析和语法分析，因为会直接在parser里进行改动，记得对parser留档以备彻底重写
## 设计架构：
### 数据层
1. ErrorType：枚举类，记录实验中要求的所有error的类型即a~m
2. Error：错误类，把错误指令封装成为一个单独的类便于使用和输出，包括了字段lineNum和ErrorType枚举类对象，分别表示错误行数和对应的种类
### 服务层
Parser：对Parser进行如下改动
1. 建立符号表：使用一个栈式符号表List<SymbolTable>，每一个子程序都单独建立一个SymbolTable，SymbolTable的tableEntry里不需要使用Reg字段，全部置为null即可，其他name、isConst和type全部需要使用，具体流程如下：
   1. 开始分析子程序的时候直接新建SymbolTable并入栈
   2. 任何操作都是操作当前栈顶的符号表
   3. 查找符号表的时候顺序向前查找
   4. 子程序结束之后将符号表从栈顶弹出去
2. 分析错误可能发生的地方：
   1. a：FormatString要求只包含数字或者32、33、40-126的ASCII字符，对于转义字符只有\n，一旦出现了其他字符就报错
   2. b：识别到在def里的ident之后需要查找符号表，出现重名则报错
   3. c：识别到在其他里的ident之后需要查找符号表，没找到则报错
   4. d：识别到调用函数之后需要查找符号表，如果type字段里的paramsType数量和输入数量不同则报错
   5. e：识别到函数调用之后需要查找符号表，如果type字段里的paramsType顺序比较类型和输入类型不同则报错
   6. f：在函数定义里，识别到return语句之后需要进行比较，如果当前函数是void并且return后存在exp节点则报错
   7. g：在函数定义里，如果当前函数不是void类型，需要判断内部是否存在return字段，不存在则报错
   8. h：在调用LVal节点的时候需要查找符号表，如果其isConst字段为true则报错
   9. i：在递归下降的过程中如果缺少右侧分号则报错
   10. j：在递归下降的过程中如果缺少右侧小括号则报错
   11. k：在递归下降的过程中如果缺少右侧中括号则报错
   12. l：在分析printf中，如果formatString中的%d数量和exp的数量不同则报错
   13. m：识别到for循环语句之后需要把标记从原状态置为true，结束之后置为原状态，识别到break或continue的时候需要判断标记是否为true，否则报错
3. 设立错误序列：使用一个栈式符号表List<Error>，记录过程中产生的所有error对象，最终顺序输出即可
4. 按照上述规则修改对应的parser的位置
### 控制层
控制层的符号表不需要做任何改动

## 关于每个节点错误的说明
1. 使用了继承属性t传递当前定义变量的类型以便于填写符号表，在BType出现的位置把t设置为VarType.i32
2. 对于数组类型ArrayType，不需要获取到具体的维度信息而只需要给出有多少维度即可，因此每一个维度都填写0
3. e类型错误中，函数参数内出现形如 !a a==b a>b 等结果类型仍认为是i32类型
4. 使用了综合属性r传递当前exp的最终类型以便于判断e类型错误，在constExp和exp两个顶层中添加了默认类型VarType.i32
5. 需要获取到r的最底层：Number(r=VarType.i32)、UnaryExp里的ident函数调用(查符号表获取FunctionType的returnType)、LVal里的ident变量调用(查符号表获取ident类型)

CompUnit：不存在直接错误
Decl：不存在直接错误
ConstDecl：缺少右侧分号，i类型错误
ConstDef：名字重定义，b类型错误；缺少右括号，k类型错误；
ConstInitVal：不存在直接错误
ConstExp：不存在直接错误
AddExp：不存在直接错误
MulExp：不存在直接错误
UnaryExp：当UnaryExp是函数调用的时候，缺少右括号，j类型错误；名字未定义，c类型错误；
UnaryOp：不存在直接错误
FuncRParams：判断是否与继承的Func内的类型一一对应，参数数量不匹配，d类型错误；参数类型不匹配，e类型错误；
PrimaryExp：缺少右侧小括号，j类型错误；
LVal：名字未定义，c类型错误；缺少右侧中括号，k类型错误；
Exp:不存在直接错误
FuncDef：名称重定义，b类型错误；是否具有返回值，g类型错误；缺少右侧小括号，j类型错误；
FuncFParams：不存在直接错误
FuncFParam：缺少右侧中括号，k类型错误；名称重定义，b类型错误；
Block：不存在直接错误
BlockItem：不存在直接错误
Stmt：
   Block：不存在直接错误
   If：缺少右侧小括号，j类型错误
   For：不存在直接错误
   Exp：不存在直接错误
   LVal = Exp：修改常量的值，h类型错误
   LVal = getint()：修改常量的值，h类型错误
   Break：非循环语句，m类型错误
   Continue：非循环语句，m类型错误
   Printf：格式字符串与表达式个数不匹配，l类型错误；格式字符串中非法字符，a类型错误
   Return：返回值的类型和函数类型是否匹配，f类型错误
ForStmt：常量不可修改，h类型错误；
Cond：不存在直接错误
LOr：不存在直接错误
LAnd：不存在直接错误
Eq：不存在直接错误
Rel：不存在直接错误
VarDecl：缺少右侧分号，i类型错误；
InitVal：不存在直接错误
MainFuncDef：是否具有返回值，g类型错误；缺少右侧小括号，j类型错误；






# 中间代码生成
## 前言
没有构建中间类IRBuilder，可以构建中间类IRBuilder让生成指令更简单，但是选择了直接在Generator里堆屎山，这样更改的粒度可以更细、能理解的知识更多，而且被深拷贝和浅拷贝搞蒙了，一开始就当c语言写的，结果发现全是指针，狗屎java...
实际上看起来的屎山全是构造指令之前的new各种指令内需要的变量，只需要一个IRBuilder，把这些指令统一生成能大幅降低代码量，更多复用性，后来幡然醒悟，用了些许IRBuilder完成了一部分的指令生成
isGlobal 和 isConst是一样的，过程中进行了一点小的优化改动把isGlobal字段名称改成了isConst，isGlobal获取的就是常量，isConst也表示这个意思
## 关于Exp的说明
### constExp和Exp
是否是const其实没有任何作用，一般表达式Exp和常量表达式constExp生成的中间代码是相同的，区别仅仅在于是否是isGlobal
1. isGlobal:
如果是全局表达式，必须要算出值并存入符号表
·使用tmpValue作为综合属性向上计算值
·不需要构造指令
·下降过程中遇到符号（一定是全局符号且不是函数）去符号表查找其值，综合属性一定是Integer类型的tmpValue
注意isGlobal线中只用到IRModule.symbolTable，因为没有basicBlock
2. !isGlobal:
如果不是全局表达式，和一般表达式一样生成指令即可
·使用tmpReg作为综合属性向上传递结果寄存器
·构造指令
·下降过程中遇到符号查找对应的寄存器，综合属性一定是Reg类型的tmpReg
注意!isGlobal线中只用到curBlock.symbolTable即可
### 递归下降的属性分析
1. tmpValue、tmpReg、tmpUpType作为综合属性，递归下降到最底端接收到输入的值之后向上传递
2. 每次向上传递一层之后，只要这层相邻节点需要计算，就把这个综合属性传给相邻节点，计算并生成指令，然后清空tmpValue/tmpReg，再作为空的综合属性递归下降到最底端获取输入，这样递归上去之后，每一层的节点都完成了计算，最终完成Exp的计算，而对于tmpUpType则是只需要识别到第一个运算符就足够了，因为一条语句中只要有一个x类型的操作数，那么这条语句就一定是x类型的，否则语句存在问题（例如i32和i1是不能执行加法的），tmpUpType比tmpValue和tmpReg简单的一点就是不需要在同一层之间进行传递，只需要到达递归终点之后获取一次就够了
3. 注意，tmpReg和tmpValue都是综合属性
·任何地方只要是在递归下降的开始，tmpReg和tmpValue都可以被设置为null（甚至任意值），之后调用递归，递归之后不能再随便设置为null
·之所以可以在递归下降的开始设置为null是因为本身就是综合属性，下降中没有用，递归之后的上升有用(这个过程包括了从底层上升以及同一层的横向传递)，而原本的值已经在递归的上一层被获取或者利用到了
4. tmpDownType和tmpOperator都是继承属性，tmpOperator会在同一层相邻成员之间从左到右继承，tmpDownType则是从上层向下层继承
5. tmpParamsType、tmpParamsName也是综合属性，在函数定义的时候用于分析参数定义情况，获取到参数的名称以及类型，用于构造函数定义指令，这里的设计还可以优化，比如使用linkedHashMap或者更改一些定义的数据结构，这里只能同时操作tmpParamsType和tmpParamsName来模拟二者是绑定的，注意tmpParamsType/Name只在FuncDef里会使用到，所以只需要在visitFuncFParams前clear即可，完成访问即可获取到综合属性
## 关于visit
### ConstDecl
定义常量：在SysY编译器的文法中，常量定义一定具有常量表达式，内部不可能含有变量（例如：const int a = b、const int a都是不合法的语句）
visitConstDecl -> visitConstDef -> visitConstInitVal -> visitConstExp
1. 在visitConstDecl里获取到tmpVarType
2. 在visitConstDef里获取到tmpIdentName
3. 在visitConstExp结束传递上来的综合属性tmpValue里获取到值
在visitConstDef里构造指令：isGlobal和!isGlobal两种情况
### VarDecl
定义变量
visitVarDecl -> visitVarDef -> \[ visitInitVal -> visitExp \]
1. 在visitVarDecl里获取到tmpVarType
2. 在visitVarDef里获取到tmpIdentName
3. 在visitExp里获取到综合属性tmpReg
在visitVarDef里构造指令：isGlobal和!isGlobal两种情况
## 特殊变量的数据流说明
### isGlobal变量的数据流分析：
在CompUnit内被置为true
在每个函数FuncDef内开始被置为false，结束被置为true
### 符号表和中间寄存器：
只有在ConstDef、FuncDef和VarDef中的符号才需要存入到符号表内，其他的中间寄存器全都不需要存储
tmpVarType可以一直使用一个，因为类型一句话中一定只有一个
identName不可以一直使用一个，因为标识符一句话中可以有多个，每一个定义语句中都需要创建一个（例如：int a = func(b, c)，递归下降func就会覆盖a）
## Function：
1. Function相关的块
FuncFParams是定义时临时的参数
FuncRParams是调用时传入的参数
FuncRParams中不需要判断isGlobal因为全局变量定义在最开始，后面才是函数，所以一定是无法调用函数的，没有isGlobal这种情况
2. FuncDef
分析一个函数的基本流程： 分析参数 -> 构造函数名称寄存器，存入全局符号表 -> 由分析结果构造需要的参数，存入到函数符号表 -> 生成函数本身定义指令
3. 库函数
printf：FormatString约束是对于转义字符，出现\除了\n没有转义的意思，出现%一定是%d（因为只有ASCII 32、33、40-126的字符不包括%、&等）
printf函数的分析流程如下：
·分析Exp，把结果存入结果数组
·顺序按照字符遍历字符串，对于每一个字符：
·如果是一般字符，调用Call指令putch函数输出
·如果是%，令i++，找到结果数组中对应的Reg，调用Call指令putint函数输出
·如果是\，并且下一位是n，令i++，调用Call指令putch输出10；如果下一位不是n，当作一般变量处理
4. 有一个很大的坑就是函数的参数在定义中定义之后，调用的时候必须alloca一个新的寄存器1，再把参数的寄存器store到1内作为这个参数最终的值，也就是使用参数的时候不能直接把参数当作一个已经定义好的符号来使用，而是需要新定义一次，否则llvm语法错误，具体步骤如下：
·获取到参数名称和列表之后，创建完新的block
·遍历整个参数名称和类型列表，对于每一项，执行以下操作：
·在block中构造一条alloca指令，用RegBuilder生成一个新的寄存器reg1，填入alloca指令
·在block中构造一条store指令，把该参数的值store到reg1内
·在block的符号表中重新添加一条表项，填入一条表项：reg1和该参数的名称、类型，覆盖掉函数中的寄存器
5. 函数中的数组参数：
数组参数通常形如int a\[]\[x]\[y]\[z]...等，是一个数组指针，也可以被理解为：
现在有许多维度为\[x]\[y]\[z]...的数组，现在有一个指针a指向了这些数组，指针每移动一位就相当于跨过了一个大小为\[x]\[y]\[z]...的数组
所以这个时候需要用到第一个参数offset了，offset给出了一共跨过了多少个数组
所以第一条GEP指令通常都是只有offset的一个偏移，用于找到目标的\[x]\[y]\[z]...数组，第二条GEP指令再去这个\[x]\[y]\[z]...数组内找对应的元素
6. 函数的LabelBuilder：每一个函数有一个labelBuilder用于自动生成一个label寄存器，每次在当前的函数内部创建一个需要分配标签的Block的时候，都需要用labelBuilder生成一个新的标签，作为这个block的label字段，注意只有需要分配标签的block使用标签生成，对于一般的block不需要生成标签
## LVal:
左值表达式有两种分歧：
1. 作为constExp和Exp体系内终结符通常是一个参与运算的变量，这时需要alloca-load-store指令，分配一个新的寄存器，把参与运算的变量的值load到新的寄存器内，在把这个寄存器里的值store到分配的寄存器里，作为综合属性传递上去参与后续运算
2. 作为Stmt中的左值表达式通常是一条赋值语句，由于赋值语句就不能再load了而是直接在原本符号寄存器上store，所以只需要一条store指令，并且也不会涉及到调用visitLVal而是直接visitExp，所以stmt里的LValAssign是不经过visitLVal的
## BasicBlock：
BasicBlock创建之后内部并没有任何指令，唯一要做的就是把curBlock指向这个block，然后继续执行程序，程序的各个visit中都会把结果指令存入到curBlock中，所以visit结束之后block中才会有一个指令列表，对于不同的基本块，只需要先创建block，然后对于不同的部分，让curBlock指向对应的创建的block，然后执行对应的visit函数递归分析即可

这里是写着写着发现了错误，BasicBLock内部可以嵌套，同时一个Function最终只会剩下一个BasicBlock，
例如在一个函数Function内functionBlock结构如下：

    代码语句块A;
    Block ---------> 
                     代码语句块B;
                     Block ---------> 
                                     代码语句块C;
                           <--------- 
                     代码语句块D;   
         <----------
    代码语句块E;

这个函数最终的整体顺序是：A->B->C->D->E
这些Block不能被顺序加入到Function的List<BasicBlock>内部，因为是嵌套关系而不是顺序关系，所以只有最外层的整体functionBlock包含了全部信息，因此Function内部的变量List<BasicBlock>可以被修改成一个单独的BasicBlock对象
再去观察这里的调用顺序，类似于一个栈式结构，并且每个Block不关心后面的定义，只关心前面的定义，但是既然使用了递归下降分析，就不需要在设置一个栈式列表了，而是需要一个指向父Block的prev指针用于递归查找符号表，并且子Block需要继承父Block的RegBuilder，否则会出现重复寄存器
最大的问题是指令的顺序问题，解决办法就是：当前Block分析内部Block，分析结束后把它的指令加入回到当前Block

由于只在Stmt内存在Block的嵌套调用，所以需要在visitStmt的case Block内执行以下详细步骤：
1. 当前Block A创建新的BasicBlock B，作为内部Block对象
2. B.prev = A，设置父级Block，用于符号表递归向上查找
3. 临时变量thisBlock = A，记录A
4. curBlock置为B
5. 让B的regBuilder = A的regBuilder
6. -----------visitBlock()，进入B的分析----------
7. curBlock = thisBlock，把curBlock设置回当前代码块A
8. curBlock.instructions.add(B.instructions)，把B的指令顺序加入到A的指令列表内

对于FuncDef中的顶层Block，执行以下步骤：
1. 创建新的Block A，作为内部Block对象
2. curBlock = A
3. curFunction.setBasicBlock(A)，加入到函数的对象中
4. -----------visitBlock()，进入内部分析----------
5. curBlock = null，完成分析函数也就结束了，后面没有其他成分了
## 关于数组的说明
### 数组定义
·数组的大小只能通过常量定义
·数组的初始值可以通过变量赋值，例如int arr[1] = {a}
·数组有多大就会有几个对应的初始赋值，即每一个数组元素都会有初始值（课程组对文法的说明）
·常量数组和变量数组是一样的指令，全局数组的初始值是数，并且指令和局部变量不一样
### 数组指令
全局数组：dso_local global \[n x i32] \[i32 x, i32 y, ...]
局部数组：先alloca \[n x i32] 大小的空间，然后用GEP指令获取到每一个元素，通过store指令赋值
分析方法：
对于全局数组：
1. 先通过左括号数量判断数组维度大小
2. 由于数组维度大小定义一定是常量数值，把isConst置为true，然后开始递归下降访问所有维度初始值Exp，结果存储在tmpValue内
3. 通过tmpValue获取到的维度值，存储在ArrayType类型的dimensions字段里，表示每个维度的大小
4. 把isConst置为true，递归下降访问所有的constInitVal，结果存储在tmpValue内，因为是全局数组
5. 把每个对应的tmpValue值存储到tableEntry的initVals内，作为一条表项存入符号表，这样这条数组表项内就有：数组名称、isConst、数组类型包括VarType和dimensions信息、存储数组的寄存器Reg、数组初始化的值initVals

对于局部数组：
1. 先通过左括号数量判断数组维度大小
2. 由于数组维度大小定义一定是常量数值，把isConst置为true，然后开始递归下降访问所有维度初始值Exp，结果存储在tmpValue内
3. 通过tmpValue获取到的维度值，存储在ArrayType类型的dimensions字段里，表示每个维度的大小
4. 把isConst置为false，因为局部数组最终初始赋值可以是变量，需要获得寄存器，递归下降访问所有的constInitVal，结果存储在tmpReg内
### 函数中的数组参数
特别的，当函数参数中含有数组的情况需要按一下方式进行讨论：
1. 函数中的数组参数是一个数组指针，原本的n维数组Arr1被下降称为n-1维数组Arr2，Arr2保留了Arr1后n-1维度的信息，取而代之的是一个指向Arr2的指针p，也就是说Arr1中原本是包含了许多Arr2数组的，现在通过让p进行偏移来访问到对应的Arr2，再去正常访问Arr2内的元素信息，于是就是两条GEP指令
2. 同样调用函数传入参数的时候也需要进行讨论，现有的n维Arr1，需要传入的m维Arr2，这时候首先就需要输入的时候输入n-m维的Arr1信息（例如：函数中是int a[]类型，而现在有int b\[1]\[2]\[3]类型，想要传入b就需要传入b\[i]\[j]）根据这n-m维信息先给b一个GEP指令进行降维，然后还需要把b转化为一个数组指针类型的变量，也就是还需要调用一次GEP指令
3. 这种文法设计方式是为了方便调用函数，这样把传入方式和形参定义方式都改成了指针对象，就可以出现上述那样把b\[i]\[j]传入给a[]类型的变量了，也就是把数组替换为起始元素位置的指针，对于高维数组非常有意义

首先需要明确GEP指令：所有的数组对象都被认为是一个指针，后面的寻址索引：<ty> <idx0>, <ty> <idx1>, ...
idx(i)表示数组在第i维上的偏移，特别的idx0表示的第0维是整个数组
例如：int a\[5]\[5]，作为一个指针，idx = 1的时候就相当于偏移到下一个a\[5]\[5]，也就是a\[25]显然越界了
每一个指针都以当前数组大小作为偏移单位，idx0 = 1就表示偏移25位，idx1 = 1就表示取a\[1]，idx = 2就表示取a\[1]\[2]
数组下降n维就需要提供n+1个idx

具体应该如何实现，取一些关键节点和普通变量进行对比分析：
1. 在FuncDef中，和普通变量一样，只需要一条alloca命令和store命令去覆盖参数列表里的寄存器，把alloca的寄存器存入符号表
2. 在FuncDef中LVal对数组变量进行调用的时候，和普通变量一样需要先load一次，然后再先GEP一次，只给出idx0的值，相当于在Arr2的数组指针中找到指定的Arr2，然后再GEP正常查找Arr2中的元素
3. 在LVal中调用数组的时候，通常会出现高维数组转化为低位数组的问题，首先需要判断输入的维度和数组本身的维度是否相同，相同则直接GEP指令，idx0=0，如果不同则需要先进行降维，根据输入的维度生成一条GEP指令进行降维，注意要正确处理降维后的数组类型

只有上述3个位置需要做GEP特殊处理，在visitFuncDef和visitLVal内做如下处理，获取到运算结束的resReg和resType
对于形参数组a[]\[2]\[3]，获取到的一定是一个指针类型，是数组指针：
1. 生成一条load指令，加载到寄存器1内
2. 如果参数序列为空（例如直接调用：a）直接跳过，resType = a.type不变
3. 如果参数序列不为空，先进行一次offset的寻址，找到第一个指针上的偏移crd.get(0)（例如：a\[1]），类型由数组指针变为数组去掉指针即可，并且取crd(1)开始到结束的参数作为新的crd，进入普通数组处理逻辑（指针 -> 数组）

对于普通数组：
1. 先判断crd中不为空，进行一次直接降维（数组 -> 数组）
2. 降维结束后如果结果是i32类型就直接load并结束
3. 否则gep i32 0, i32 0再降一维度（数组 -> 指针）

这样也就相当于如果LVal中出现了数组元素，对于形参类型，额外需要一条load和一条GEP成为普通数组，对于普通数组，如果输入维度不是空的就需要对数组一条GEP进行降维获取到降维结束后的数组类型type，如果type只剩下一个i32就执行load结束，如果type还是一个数组就降一维成为指针
所以再LVal中调用数组Exp，如果是一个元素就会用load加载出来，如果是一个数组就会下降为指针，注意额外处理形参数组即可
## 跳转stmt指令
### if语句的处理
对于if语句，首先需要处理trueBlock、falseBlock、finalBlock，其次是处理Cond
1. 先根据if语句中是否有else来创建true、false、finalBlock，并为他们生成label标签
2. 处理cond语句，每一个LOr和LAnd都作为一个block，通过继承属性tmpBlock和tmpReg来传递br指令和运算结果
3. 处理结束cond之后处理if和else内的stmt语句，分别使用trueBlock和falseBlock即可，处理结束在这两个Block最后添加br指令跳转到finalBlock内
4. 注意finalBlock是结束if-else语句之后，本身应当是和开始if-else之前的代码属于相同基本块，但为了标签只能作为一个新的基本块，但是可以让finalBlock内只有一个标签指令，然后直接把finalBlock并回到原本代码的基本块内

在LOr和LAnd内部对block的处理：
1. 每一个LAnd或者LOr节点都单独构建一个BasicBlock，这样做的目的是完成短路求值
2. 首先进入的是LOr->LAnd，对于一串LAnd进行的与运算，只需要顺序遍历，只要有一个是0就代表结果一定是0完成短路，直接跳转到falseBlock，如果能够一直遍历到最后一个LAnd，说明都不是0那么一定是1，返回给LOr的话这一条LOr的值就是1，那么LOr出现短路，直接跳转到trueBlock，而如果LOr访问到了最后一个，说明前面都不是1，总的运算结果也就是0跳转到falseBlock
3. 对于每个LOr，其本身不需要生成指令，因为它由LAnd构成，按照上一条，它只需要遍历LAnd节点即可，如果不是最后一个，就把nextLOrBlock设置为下一个LOr节点的地址，如果是最后一个，就把nextLOrBlock设置为ifFalseBlock
4. 对于每个LAnd，falseBlock = nextLOrBlock，如果不是最后一个，就让trueBlock = nextLAndBlock，如果是最后一个，就让trueBlock = ifTrueBlock，递归下降获取到eqExp的运算结果类型为i1的寄存器tmpReg，直接构造一条br指令即可
### For语句的处理
关于For的流程顺序：
                forStmt1 -> cond -> stmt -> forStmt2
                              ^                |
                              | <---------------  
forStmt1中最后无条件跳转到cond内
进入cond之后进行判断，条件为真就进入stmt，为假就直接结束进入finalBlock
stmt中最后无条件跳转到forStmt2内
forStmt2中最后无条件跳转到cond内

注意如果出现了break或者continue就不需要在指令结束之后加br了，因为已经有了br，后面再加br是无效代码llvm会报错，而只有stmt中可能会出现break或者continue，所以只要对visitStmt部分添加判断即可
### 关于if和for语句的跳转
对于llvm编译器来讲，br/ret标签后面不能再有br/ret标签了，所以如果for、if内部存在了跳转语句，那么最后就不再需要添加一条br指令跳转到finalBlock了，因此需要设置一个全局变量jump，对于return、break、continue语句作为一个综合属性，除此之外，当前块内跳转语句后面的其他语句都不需要再被记录了，因为他们永远不会被执行到，这个时候可以把当前块的指令列表上锁，向里面添加指令是无效的
1. 进入一个if/else/for块内的时候，首先记录jump，然后把jump置为false，默认当前块内没有跳转，然后执行递归下降分析
2. 分析到return/break/continue指令之后，把jump置为true表示发生了跳转，并把当前指令列表上锁，让后续指令都失效不添加，跳转指令是最后一条
3. visit一个需要跳转的语句块（例如：if for中的stmt）结束之后，判断jump是否为true，若是说明内部已经有了跳转指令，就不需要额外再添加跳转的目的地址finalBlock了，若否则再添加一条跳转指令目的地址为finalBlock
4. 在访问if/else/for块结束的时候，再把jump置为开始时记录的值，这样对于外部来说，访问这个块完全不影响原本的jump值，从而避免了内外之间相互影响的问题
关于指令列表的上锁，使用了Collections.unmodifiableList，是不可修改的，如果修改就会报错，所以再添加一条try-catch，遇到报错直接跳过

