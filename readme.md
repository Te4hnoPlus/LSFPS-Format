## LSFPS-Format
Мощная мини-библиотека для форматирования текста

### Использование
Для начала работы необходимо определиться с возможными входными.
данным, которые нужно отформатировать. Типов входных данных 
может быть несколько, для каждого из них нужно создать свой провайдер:
```java
final MapVarProvider<User> PROVIDER = new MapVarProvider<>(User.class);
//Пример типа данных
class User{
    String name;
    User parent;
    int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
```
После того, как провайдер данных создан, можно приступить к созданию формата.
Для создания формата используется `FormatBuilder`:
```java
FormatBuilder<User> builder = new FormatBuilder<>(User.class)
    .setProvider(PROVIDER).fixEmpty()
    .setFormat("User-%age%: %name%, parent-%parent.age%: %parent/name%");

Formatter<User> format = fmBuilder.build();
```
После создания, формат уже не может и не должен быть изменен.

Создание формата - процесс тяжелый, 
предполагается что это будет выполнено заранее, до форматирования данных.

Теперь можно приступить к форматированию:
```java
User user1 = new User("Homa"  , 1);
User user2 = new User("Te4hno", 5);

user1.parent = user2;

System.out.println(format.get(user1));
```
Результат форматирования - `User-1: Homa, parent-5: Te4hno`