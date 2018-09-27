package com.github.upatovav.mlmGroupValue;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
public class User {

    @NonNull
    private long id;

    private List<User> children = new ArrayList<>();

    @NonNull
    private BigDecimal value;

    private BigDecimal go = BigDecimal.ZERO;

}
