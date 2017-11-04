.class public programa1
.super java/lang/Object

.field static data [I
.field static mx I = 1
.field static mn I
.field static c [I
.field static x [I

.method static public <clinit>()V
.limit stack 1  
.limit locals 0

bipush 100
newarray int
putstatic programa1/data [I

bipush 12
newarray int
putstatic programa1/c [I

return
.end method

.method public static det([I)I
.limit stack 1  
.limit locals 2  





iload_1
ireturn
.end method

.method public static f2(I)I
.limit stack 1  
.limit locals 2  







iload_1
ireturn
.end method

.method public static main([Ljava/lang/String;)V
.limit stack 0  
.limit locals 0  





return
.end method

