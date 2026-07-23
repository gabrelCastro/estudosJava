# flatMap

Estudos sobre `flatMap` em Java (Stream e Optional).

## A ideia em uma frase

`map` transforma 1 elemento em 1 elemento. `flatMap` transforma 1 elemento em
um *stream* de N elementos (0, 1 ou muitos) e concatena todos num stream só.

```
map     : Stream<A> --(A -> B)--------> Stream<B>
flatMap : Stream<A> --(A -> Stream<B>)-> Stream<B>   // sem o aninhamento
```

## Regra prática

| A função que você passa devolve... | Use       |
|------------------------------------|-----------|
| um valor único                     | `map`     |
| um `Stream` / coleção              | `flatMap` |
| um `Optional`                      | `flatMap` |

Se o resultado ficou `Stream<List<X>>` ou `Optional<Optional<X>>`, era `flatMap`.

## Arquivos

| Arquivo | Conteúdo |
|---------|----------|
| `MapVsFlatMap.java` | A diferença fundamental; flatMap filtrando (`Stream.empty()`) e multiplicando elementos |
| `FlatMapColecoes.java` | O caso do dia a dia: `Pedido` com lista de `Item`; agregações, `groupingBy`, preservar o objeto pai |
| `FlatMapOptional.java` | `Optional.flatMap` para navegar objetos aninhados sem `if != null`; `Optional.stream()` |
| `FlatMapAvancado.java` | Arrays, `flatMapToInt`, split de strings, produto cartesiano, leitura de arquivos, armadilhas e `mapMulti` |

## Como rodar

Cada arquivo é independente e tem seu próprio `main`:

```bash
java MapVsFlatMap.java
java FlatMapColecoes.java
java FlatMapOptional.java
java FlatMapAvancado.java
```

Ou compilando tudo de uma vez:

```bash
javac -d out *.java && java -cp out MapVsFlatMap
```

Testado no OpenJDK 21. Usa `record`, `toList()` e `mapMulti`, então precisa de Java 16+.
