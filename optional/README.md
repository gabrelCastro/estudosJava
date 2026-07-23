# Optional

Estudos sobre `java.util.Optional`.

## Para que serve

`Optional<T>` é uma caixa que contém um valor **ou** está vazia. O propósito é
tornar explícito na assinatura do método que o retorno pode não existir, em vez
de devolver `null` e depender de quem chama lembrar de checar.

> Optional não existe para eliminar o `null` do sistema inteiro — existe para ser
> um **tipo de retorno honesto**.

## Regra de ouro

| Situação | Recomendação |
|---|---|
| Retorno de método que pode não achar nada | ✅ `Optional<T>` |
| Campo de classe | ❌ campo normal + getter devolvendo `Optional` |
| Parâmetro de método | ❌ aceite `null` ou faça sobrecarga |
| Retorno de coleção | ❌ devolva lista vazia, nunca `Optional<List<T>>` |

## Como desempacotar

| Quero... | Use |
|---|---|
| um padrão constante | `orElse(x)` |
| um padrão caro de calcular | `orElseGet(() -> x)` |
| falhar se não tiver | `orElseThrow(...)` |
| executar algo só se tiver | `ifPresent(...)` |
| tratar os dois casos | `ifPresentOrElse(...)` |
| transformar o conteúdo | `map` / `flatMap` |
| tentar outra fonte | `or(() -> outroOptional)` |
| jogar na Stream API | `.stream()` |

`isPresent()` + `get()` é o antipadrão principal: só reescreve o `if != null`
com mais cerimônia. E `get()` está marcado para depreciação — prefira
`orElseThrow()`, que ao menos avisa no nome que pode explodir.

`map` vs `flatMap`: se a função devolve o valor cru, `map`; se ela já devolve um
`Optional`, `flatMap` (senão você fica com `Optional<Optional<T>>`). Mesma lógica
da pasta [`../flatMap`](../flatMap).

## Arquivos

| Arquivo | Conteúdo |
|---|---|
| `CriandoOptional.java` | `of` / `ofNullable` / `empty`, a NPE do `of(null)`, o problema que o Optional resolve, APIs do JDK que já devolvem Optional |
| `ConsumindoOptional.java` | `orElse` vs `orElseGet` (com a armadilha da avaliação ansiosa), `orElseThrow`, `ifPresent`, `ifPresentOrElse` e por que evitar `isPresent()` + `get()` |
| `TransformandoOptional.java` | `map`, `flatMap`, `filter`, `or` em cascata de fallbacks, `Optional.stream()`, imperativo vs declarativo |
| `OptionalNaPratica.java` | Repository/Service com o uso correto, os 7 antipadrões, Optional e coleções, `OptionalInt`/`OptionalDouble` |

## Como rodar

Cada arquivo é independente e tem seu próprio `main`:

```bash
java CriandoOptional.java
java ConsumindoOptional.java
java TransformandoOptional.java
java OptionalNaPratica.java
```

Ou compilando tudo de uma vez:

```bash
javac -d out *.java && java -cp out CriandoOptional
```

Testado no OpenJDK 21. Usa `record`, text blocks e `formatted`, então precisa de Java 16+.
