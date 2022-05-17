import React from 'react';
import {
    Chart,
    ChartAxis,
    ChartGroup,
    ChartLine,
    ChartScatter,
    ChartThemeColor,
} from '@patternfly/react-charts';
import {Title} from "@patternfly/react-core";

export const TrialHistory = ({experiment}) => {
    const trial_history = experiment.trialHistory;

    let series = [
        {
            datapoints: [] ,
            legendItem: {name: 'Trials'}
        },
    ];

    if ( trial_history != undefined) {
        let arr = [];
        Object.keys(trial_history).map(function(key){
            arr.push({[key]:trial_history[key]})
            return arr;
        });

        let mappedDatapoints = arr.map( (result, index) => ({name: "Trials", x: index, y: result[index].value}));
        // console.log(mappedDatapoints);
        series = [
            {
                datapoints: mappedDatapoints,
                legendItem: {name: 'Trials'}
            },
        ];
    }

    return (
            <div style={{height: '275px'}}>
                <Title headingLevel={"h2"}>Trial History</Title>
                <Chart
                    ariaDesc="Average number of pets"
                    ariaTitle="Line chart example"
                    // containerComponent={
                    //     <ChartVoronoiContainer
                    //         labels={({datum}) => datum.childName.includes('line-') ? `${datum.name}: ${datum.y}` : null}
                    //         constrainToVisibleArea
                    //     />
                    // }
                    legendData={series.map(s => s.legendItem)}
                    legendPosition="bottom-left"
                    height={275}
                    maxDomain={{y: 100}}
                    minDomain={{y: 0}}
                    padding={{
                        bottom: 75, // Adjusted to accommodate legend
                        left: 50,
                        right: 50,
                        top: 50
                    }}
                    themeColor={ChartThemeColor.orange}
                    width={1400}
                >
                    <ChartAxis tickValues={[0, 20, 40, 60, 80, 100]}/>
                    <ChartAxis dependentAxis showGrid tickValues={[0, 20, 40, 60, 80, 100]}/>
                    <ChartGroup>
                        {series.map((s, idx) => {
                            return (
                                <ChartScatter
                                    data={s.datapoints}
                                    key={'scatter-' + idx}
                                    name={'scatter-' + idx}
                                />
                            );
                        })}
                    </ChartGroup>
                    <ChartGroup>
                        {series.map((s, idx) => {
                            return (
                                <ChartLine
                                    key={'line-' + idx}
                                    name={'line-' + idx}
                                    data={s.datapoints}
                                />
                            );
                        })}
                    </ChartGroup>
                </Chart>
            </div>
    );
}