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
Если стандартного доступа по полям не достаточно, можно дополнить провайдер своими переменными:
Переопределим метод `MapVarProvider#init` и зарегистрируем несколько переменных

```java
@Override
protected void init() {
    //в регистрации данных переменных нет прямой необходимости, они создадутся 
    //и зарегистрируются автоматически, при запросе к полям `name` и `parent`
    reg("name"  , (IVarGetter.Str<Examples.User>) user -> user.name);
    reg("parent", new IVarGetter<Examples.User>() {
        public Object get(User user) {
            return user.parent;
        }
        public Class<?> getType() {
            return Examples.User.class;
        }
    });
}
```
Так же, после создания можно дополнительно регистрировать переменные:
```java
PROVIDER.section("func")
    .reg("has-parent", (IVarGetter.Bool<User>) user -> user.parent != null)
    .reg("an-adult", (IVarGetter.Bool<User>) user -> user.age >= 18)
    .back().section("extra")
    .reg("name-caps", (IVarGetter.Str<User>) user -> user.name.toUpperCase(Locale.ROOT));
```

После того, как провайдер данных создан, можно приступить к созданию формата.
Для создания формата используется `FormatBuilder`:
```java
FormatBuilder<User> builder = new FormatBuilder<>(User.class)
    .setProvider(PROVIDER).fixEmpty()
    .setFormat("User-%age%: %name%, parent-%parent.age%: %parent/name%");
```
При необходимости, можно изменить спец-символы форматирования:
```java
builder.setMath('<', '>', '\\');
```

У FormatBuilder может быть несолько разных провайдеров данных, 
использование того или иного провайдера будет выбрано автоматически, 
исходя из типа входного аргумента:
```java
builder.setProvider(PROVIDER, PROVIDER_2, PROVIDER_3 ...);
```

Теперь можно создать формат
```java
Formatter<User> format = builder.build();
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